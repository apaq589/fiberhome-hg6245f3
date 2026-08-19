package com.fiberhome.control;
import android.app.Activity; import android.os.Bundle; import android.view.View; import android.webkit.*; import android.widget.*;
public class MainActivity extends Activity {
 EditText ip; WebView web;
 public void onCreate(Bundle b){super.onCreate(b);setContentView(R.layout.activity_main); ip=findViewById(R.id.ipEdit); web=findViewById(R.id.webView);
 web.setWebViewClient(new WebViewClient()); WebSettings s=web.getSettings(); s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true); s.setDatabaseEnabled(true);
 findViewById(R.id.loginBtn).setOnClickListener(v->open("")); findViewById(R.id.wifiBtn).setOnClickListener(v->open("wifi"));
 findViewById(R.id.accountBtn).setOnClickListener(v->open("account")); findViewById(R.id.clientBtn).setOnClickListener(v->open("client"));
 findViewById(R.id.rebootBtn).setOnClickListener(v->open("reboot")); findViewById(R.id.fullBtn).setOnClickListener(v->open(""));
 }
 String base(){String x=ip.getText().toString().trim(); if(x.isEmpty())x="192.168.1.1"; if(!x.startsWith("http"))x="http://"+x; return x;}
 void open(String a){web.setVisibility(View.VISIBLE); web.loadUrl(base()+"/normal"); if(!a.isEmpty())web.postDelayed(()->web.evaluateJavascript(js(a),null),2500);}
 String js(String a){String x="(function(){function E(){return Array.from(document.querySelectorAll('*')).filter(e=>(e.innerText||'').trim().length>0)}function C(w){for(let e of E()){let t=(e.innerText||'').trim().toLowerCase();if(w.some(q=>t===q||t.includes(q))){e.click();return}}}";
 if(a.equals("wifi"))x+="setTimeout(()=>C(['network']),200);setTimeout(()=>C(['wlan settings']),900);setTimeout(()=>C(['2.4g advanced','2.4g advance','advanced']),1600);";
 if(a.equals("account"))x+="setTimeout(()=>C(['management']),200);setTimeout(()=>C(['account management']),900);setTimeout(()=>C(['user account']),1600);";
 if(a.equals("client"))x+="setTimeout(()=>C(['status']),200);setTimeout(()=>C(['dhcp']),900);";
 if(a.equals("reboot"))x+="setTimeout(()=>C(['management']),200);setTimeout(()=>C(['device management']),900);setTimeout(()=>C(['device reboot','reboot']),1600);";
 return x+"})()";}
 public void onBackPressed(){if(web.getVisibility()==View.VISIBLE&&web.canGoBack())web.goBack();else if(web.getVisibility()==View.VISIBLE)web.setVisibility(View.GONE);else super.onBackPressed();}
}