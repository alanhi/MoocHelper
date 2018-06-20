package me.shl.moochelper;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import me.shl.tools.webTools;

import android.webkit.JavascriptInterface;

/**
 * Created by SHL on 2018/5/5.
 */
public class MainActivity extends AppCompatActivity {
    private String ItemId = "0";
    private String cookie = "0";

    //    JavaScript对象注入
    final class InJavaScriptLocalObj {
        @JavascriptInterface
        @SuppressLint("unused")
        public void showId(String html) {
            ItemId = html;
            Toast.makeText(MainActivity.this, "发现课程ID：" + html, Toast.LENGTH_SHORT).show();
        }
    }

    //    线程中使用的toast事件
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            String str = (String) msg.obj;
            Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }


    //    初始化webView
    @SuppressLint({"SetJavaScriptEnabled"})
    private void init() {
        WebView webView = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new InJavaScriptLocalObj(), "localObj");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
                Map<String, String> extraHeaders = new HashMap<String, String>();
                if (view.getUrl() != null) extraHeaders.put("Referer", view.getUrl());
                view.loadUrl(url, extraHeaders);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (url.startsWith("http://yit.minghuaetc.com/study/unit")) {
                    CookieManager cookieManager = CookieManager.getInstance();
                    cookie = cookieManager.getCookie(url);
                    view.loadUrl("javascript:window.localObj.showId(document.getElementById('itemId').value);");
                }
                super.onPageFinished(view, url);
            }
        });
        webView.loadUrl("http://yit.minghuaetc.com");
    }

    //    返回键监听
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            WebView webView = (WebView) findViewById(R.id.webview);
            if (webView.canGoBack()) {
                webView.goBack();//返回上一页面
                return true;
            } else {
                System.exit(0);//退出程序
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    //    添加菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    //    菜单监听
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_start:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        webTools.startID = Integer.valueOf(ItemId);
                        Message msg = new Message();
                        msg.obj = "首课程ID为: " + webTools.startID;
                        handler.sendMessage(msg);
                    }
                }).start();
                break;
            case R.id.item_end:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        webTools.endID = Integer.valueOf(ItemId);
                        Message msg = new Message();
                        msg.obj = "尾课程ID为: " + webTools.endID;
                        handler.sendMessage(msg);
                    }
                }).start();
                break;
            case R.id.item_run:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Message msg = new Message();
                        msg.obj = "任务开始，请稍后";
                        handler.sendMessage(msg);
                        Message msg1 = new Message();
                        if (webTools.taskRun(cookie)) {
                            msg1.obj = "任务结束，请检查";
                            handler.sendMessage(msg1);
                        } else {
                            msg1.obj = "错误：首尾课程ID可能有误";
                            handler.sendMessage(msg1);
                        }
                    }
                }).start();
                break;
            case R.id.item_redpacket:
                redpacket();
                break;
            case R.id.item_donate:
                AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
                b.setTitle("捐赠");
                b.setMessage("写代码不易，要请作者吃包辣条吗？");
                b.setNegativeButton("取消", null);
                b.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        donate();
                    }
                });
                b.create();
                b.show();
                break;
            case R.id.item_help:
                Intent intent1 = new Intent();
                intent1.setClass(this, HelpActivity.class);
                startActivity(intent1);
                break;
            default:
                break;
        }
        return true;
    }

    //    捐赠
    public void donate() {
        Intent intent = new Intent("android.intent.action.VIEW");
        Uri uri = null;
        try {
            uri = Uri.parse("alipays://platformapi/startapp?saId=10000007&qrcode=HTTPS://QR.ALIPAY.COM/FKX09242HEJNNM5Q0RFVA4");
        } catch (Exception e) {
            Toast.makeText(this, "解析失败", Toast.LENGTH_SHORT).show();
        }
        intent.setData(uri);
        try {
            startActivity(intent);
        } catch (Exception e2) {
            Toast.makeText(this, "拉起失败，没有安装支付宝客户端T_T", Toast.LENGTH_SHORT).show();
        }
    }

    //    支付宝红包
    public void redpacket() {
        Intent intent = new Intent("android.intent.action.VIEW");
        Uri uri = null;
        try {
            uri = Uri.parse("alipays://platformapi/startapp?saId=10000007&qrcode=https://qr.alipay.com/c1x06626vtdid674xsuqh84");
        } catch (Exception e) {
            Toast.makeText(this, "解析失败", Toast.LENGTH_SHORT).show();
        }
        intent.setData(uri);
        try {
            startActivity(intent);
        } catch (Exception e2) {
            Toast.makeText(this, "拉起失败，没有安装支付宝客户端T_T", Toast.LENGTH_SHORT).show();
        }
    }

}
