package com.fiberhome.control;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

public class MainActivity extends Activity {
    EditText ip; WebView web;
    boolean triedHttps = false;
    boolean triedHttp = false;

    @Override
    public void onCreate(Bundle b){
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        ip = findViewById(R.id.ipEdit);
        web = findViewById(R.id.webView);

        web.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url){
                triedHttp = url != null && url.startsWith("http://");
                triedHttps = url != null && url.startsWith("https://");
            }

            @Override
            @Deprecated
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl){
                handleLoadError(failingUrl, errorCode, description);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error){
                if(request.isForMainFrame()){
                    handleLoadError(request.getUrl().toString(), error.getErrorCode(), error.getDescription().toString());
                }
            }

            @Override
            public void onReceivedSslError(WebView view, android.webkit.SslErrorHandler handler, android.net.http.SslError error){
                // WARNING: Insecure. Bypass SSL errors for local testing only.
                // Proceed despite SSL errors so self-signed / mismatched certs won't block testing.
                Log.w("MainActivity","SSL error loading page (bypassing): " + error.toString());
                handler.proceed();
            }

            private void handleLoadError(String failingUrl, int errorCode, String description){
                if(failingUrl == null) return;
                String lower = failingUrl.toLowerCase();
                if(lower.startsWith("http://") && !triedHttps){
                    triedHttps = true;
                    String alt = failingUrl.replaceFirst("(?i)http://","https://");
                    web.post(() -> web.loadUrl(alt));
                } else if(lower.startsWith("https://") && !triedHttp){
                    triedHttp = true;
                    String alt = failingUrl.replaceFirst("(?i)https://","http://");
                    web.post(() -> web.loadUrl(alt));
                }
            }
        });

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            s.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        }

        findViewById(R.id.loginBtn).setOnClickListener(v->open(""));
        findViewById(R.id.wifiBtn).setOnClickListener(v->open("wifi"));
        findViewById(R.id.accountBtn).setOnClickListener(v->open("account"));
        findViewById(R.id.clientBtn).setOnClickListener(v->open("client"));
        findViewById(R.id.blockBtn).setOnClickListener(v->open("block"));
        findViewById(R.id.rebootBtn).setOnClickListener(v->open("reboot"));
        findViewById(R.id.fullBtn).setOnClickListener(v->open(""));
    }

    String base(){
        String x = ip.getText().toString().trim();
        if(x.isEmpty()) x = "192.168.1.1";
        if(x.startsWith("http://") || x.startsWith("https://")){
            if(x.endsWith("/")) x = x.substring(0, x.length()-1);
            return x;
        }
        return "http://"+x;
    }

    void open(String a){
        web.setVisibility(View.VISIBLE);
        triedHttp = false; triedHttps = false;
        String base = base();
        String url = base.endsWith("/") ? base + "normal" : base + "/normal";
        web.loadUrl(url);
        if(!a.isEmpty()) web.postDelayed(() -> web.evaluateJavascript(js(a), null), 2500);
    }

    String js(String a){
        String x = "(function(){function E(){return Array.from(document.querySelectorAll('*')).filter(e=>(e.innerText||'').trim().length>0)}function C(w){for(let e of E()){let t=(e.innerText||'').trim().toLowerCase();if(w.some(q=>t===q||t.includes(q))){e.click();return}}}";
        if(a.equals("wifi")) x += "setTimeout(()=>C(['network']),200);setTimeout(()=>C(['wlan settings']),900);setTimeout(()=>C(['2.4g advanced','2.4g advance','advanced']),1600);";
        if(a.equals("account")) x += "setTimeout(()=>C(['management']),200);setTimeout(()=>C(['account management']),900);setTimeout(()=>C(['user account']),1600);";
        if(a.equals("client")) x += "setTimeout(()=>C(['status']),200);setTimeout(()=>C(['dhcp']),900);";
        if(a.equals("reboot")) x += "setTimeout(()=>C(['management']),200);setTimeout(()=>C(['device management']),900);setTimeout(()=>C(['device reboot','reboot']),1600);";
        if(a.equals("block")){
            x += "setTimeout(()=>C(['status']),200);setTimeout(()=>C(['dhcp']),900);";
            x += "setTimeout(()=>{(function(){var terms=['unknown','unnamed','-','guest','no name','n/a'];function text(n){return (n.innerText||'').trim().toLowerCase();}var rows=document.querySelectorAll('tr, li, div');for(var i=0;i<rows.length;i++){var r=rows[i];var t=text(r);var parts=t.split('.');var isIp=false;if(parts.length==4){isIp=true;for(var j=0;j<4;j++){if(isNaN(parseInt(parts[j]))){isIp=false;break;}}}if(terms.some(function(tt){return t.includes(tt);})||isIp){var btn=r.querySelector('button,input[type=button],a');if(btn){try{btn.click();console.log('Block action clicked for:',t);}catch(e){console.log('Click failed',e);}break;}}}})()},1600);";
        }
        return x+"})()";
    }

    @Override
    public void onBackPressed(){
        if(web.getVisibility()==View.VISIBLE && web.canGoBack()) web.goBack();
        else if(web.getVisibility()==View.VISIBLE) web.setVisibility(View.GONE);
        else super.onBackPressed();
    }
}
