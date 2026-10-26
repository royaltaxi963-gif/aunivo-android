package com.aunivo.app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int MIC_REQUEST = 204;
    private WebView webView;
    private SpeechRecognizer recognizer;
    private boolean pendingStart = false;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        webView.setBackgroundColor(0xFF05060B);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public void onPermissionRequest(PermissionRequest request) {
                runOnUiThread(() -> request.grant(request.getResources()));
            }
        });
        webView.addJavascriptInterface(new NativeBridge(), "AunivoAndroid");
        setContentView(webView);
        webView.loadUrl("file:///android_asset/index.html");
    }

    private class NativeBridge {
        @JavascriptInterface public void startListening() {
            runOnUiThread(() -> {
                if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    pendingStart = true;
                    requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, MIC_REQUEST);
                } else startNativeRecognition();
            });
        }
        @JavascriptInterface public void stopListening() {
            runOnUiThread(() -> {
                pendingStart = false;
                if (recognizer != null) {
                    try { recognizer.cancel(); recognizer.destroy(); } catch (Exception ignored) {}
                    recognizer = null;
                }
            });
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == MIC_REQUEST) {
            if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED && pendingStart) {
                pendingStart = false;
                startNativeRecognition();
            } else {
                pendingStart = false;
                js("window.AunivoNativeError && window.AunivoNativeError('not-allowed')");
                Toast.makeText(this, "Aunivo uchun mikrofon ruxsati kerak", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startNativeRecognition() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            js("window.AunivoNativeError && window.AunivoNativeError('unavailable')");
            return;
        }
        if (recognizer != null) {
            try { recognizer.destroy(); } catch (Exception ignored) {}
        }
        recognizer = SpeechRecognizer.createSpeechRecognizer(this);
        recognizer.setRecognitionListener(new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle params) { js("window.AunivoNativeStarted && window.AunivoNativeStarted()"); }
            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {}
            @Override public void onError(int error) {
                recognizer = null;
                js("window.AunivoNativeError && window.AunivoNativeError('recognition-error')");
            }
            @Override public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                recognizer = null;
                if (matches != null && !matches.isEmpty()) {
                    String text = matches.get(0).replace("\\", "\\\\").replace("'", "\\'").replace("\n", " ").replace("\r", " ");
                    js("window.AunivoNativeResult && window.AunivoNativeResult('" + text + "')");
                } else js("window.AunivoNativeError && window.AunivoNativeError('empty')");
            }
            @Override public void onPartialResults(Bundle partialResults) {}
            @Override public void onEvent(int eventType, Bundle params) {}
        });
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "uz-UZ");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "uz-UZ");
        intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);
        try { recognizer.startListening(intent); }
        catch (Exception e) { recognizer = null; js("window.AunivoNativeError && window.AunivoNativeError('start-failed')"); }
    }

    private void js(String script) {
        runOnUiThread(() -> { if (webView != null) webView.evaluateJavascript(script, null); });
    }

    @Override public void onBackPressed() {
        if (webView != null) webView.evaluateJavascript("window.dispatchEvent(new Event('aunivoandroidback'))", null);
        super.onBackPressed();
    }

    @Override protected void onDestroy() {
        if (recognizer != null) { try { recognizer.destroy(); } catch (Exception ignored) {} recognizer = null; }
        if (webView != null) { webView.removeJavascriptInterface("AunivoAndroid"); webView.destroy(); webView = null; }
        super.onDestroy();
    }
}
