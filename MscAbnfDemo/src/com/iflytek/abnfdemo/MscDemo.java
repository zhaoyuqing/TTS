/*
 * Copyright (C) 2009-2010, iFLYTEK Ltd.
 *
 * All rights reserved.
 *
 */
package com.iflytek.abnfdemo;

import java.io.InputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.iflytek.speech.*;
import com.iflytek.ui.RecognizerDialog;
import com.iflytek.ui.RecognizerDialogListener;

/**
 * @author iflytek
 */
public class MscDemo extends Activity implements OnClickListener {
	
	private final String APP_ID = "4d6774d0";
	private final static String KEY_GRAMMAR_ID = "grammar_id";
	private RecognizerDialog recognizerDialog = null;
	private String grammarText = null;
	private String grammarID = null;
	private Toast mToast;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);
		mToast = Toast.makeText(this,"",Toast.LENGTH_LONG);
		recognizerDialog = new RecognizerDialog(this,"appid="+APP_ID);
		SpeechUser.getUser().login(this, null, null, "appid="+APP_ID, loginListener);
		
		// 读取保存的语法ID
		SharedPreferences preference = this.getSharedPreferences("abnf",MODE_PRIVATE);
		grammarID =preference.getString(KEY_GRAMMAR_ID, null);
		
		// 显示语法内容
		grammarText = readAbnfFile();
		
		EditText tmsg = (EditText)findViewById(R.id.edit_text);
		tmsg.setText(grammarText);
		
		findViewById(R.id.btn_recognize).setOnClickListener(this);
		findViewById(R.id.btn_upload).setOnClickListener(this);
		findViewById(R.id.btn_recognizeGrammar).setOnClickListener(this);
	} 
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onClick(View v) {
		
		switch(v.getId())
		{
		case R.id.btn_recognize:
			EditText tmsg = (EditText)findViewById(R.id.edit_text);
			tmsg.setText(grammarText);
			recognizerDialog.setListener(mRecoListener);
			recognizerDialog.setEngine(null, "grammar_type=abnf", grammarText);
			recognizerDialog.show();
			break; 
		case R.id.btn_upload:
			try {
					//上传数据格式gb2312
					byte[] datas = grammarText.getBytes("gb2312");
					DataUploader uploader = new DataUploader();
					uploader.uploadData(this, uploadListener, "abnf", "subject=asr,data_type=abnf",datas);
					mToast.setText("开始上传语法....");
					mToast.show();
				} catch (Exception e) {
					e.printStackTrace();
			}
			break;
		case R.id.btn_recognizeGrammar:
			if(TextUtils.isEmpty(grammarID))
			{
				mToast.setText("上传语法后才可以使用.");
				mToast.show();
			}else
			{
				tmsg = (EditText)findViewById(R.id.edit_text);
				tmsg.setText(grammarText);
				recognizerDialog.setListener(mRecoListener);
				recognizerDialog.setEngine("asr", null, grammarID);
				recognizerDialog.show();	
			}
			break; 
		default:
			break;
		}
	}
	
	/**
	 * 识别监听回调
	 */
	private RecognizerDialogListener mRecoListener = new RecognizerDialogListener()
	{
		@Override
		public void onResults(ArrayList<RecognizerResult> results,boolean isLast) {
			String text = "";
			for(int i = 0; i < results.size(); i++)
			{
				RecognizerResult result = results.get(i);
				text += result.text + " confidence=" + result.confidence + "\n";
			}
			EditText tmsg = (EditText)findViewById(R.id.edit_text);
			tmsg.setText(text);
			tmsg.setSelection(tmsg.getText().length());
		}

		@Override
		public void onEnd(SpeechError error) {
		}
	};
	
	/**
	 * 读取语法文件
	 * @return
	 */
	private String readAbnfFile()
	{
		int len = 0;
		byte []buf = null;
		String grammar = "";
		try {
			InputStream in = getAssets().open("gm_continuous_digit.abnf");			
			len  = in.available();
			buf = new byte[len];
			in.read(buf, 0, len);
			
			grammar = new String(buf,"gb2312");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return grammar;

	}
	
	/**
	 * 用户登录回调监听器.
	 */
	private SpeechListener loginListener = new SpeechListener()
	{

		@Override
		public void onData(byte[] arg0) {
		}

		@Override
		public void onEnd(SpeechError error) {
			if(error != null) 
			{
				mToast.setText("登录失败");
				mToast.show();
			}else
			{
				mToast.setText("登录成功");
				mToast.show();
			}
		}

		@Override
		public void onEvent(int arg0, Bundle arg1) {
		}		
	};
	
	/**
	 * 上传语法文件 回调监听器.
	 */
	SpeechListener  uploadListener = new SpeechListener()
	{
		@Override
		public void onEnd(SpeechError arg0) {
			if(arg0 != null)
			{
				mToast.setText(arg0.toString());
				mToast.show();
			}
		}

		@Override
		public void onData(byte[] arg0) {
			grammarID = new String(arg0);
			// 保存语法ID
			SharedPreferences preference = MscDemo.this.getSharedPreferences("abnf",MODE_PRIVATE);
			preference.edit().putString(KEY_GRAMMAR_ID,grammarID).commit();
			
			mToast.setText("语法文件ID：" + grammarID);
			mToast.show();
		}

		@Override
		public void onEvent(int arg0, Bundle arg1) {
		}
	};
}