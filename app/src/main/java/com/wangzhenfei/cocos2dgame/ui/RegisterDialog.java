package com.wangzhenfei.cocos2dgame.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.ui.ImageGridActivity;
import com.lzy.imagepicker.view.CropImageView;
import com.wangzhenfei.cocos2dgame.ImagePickerImageLoader;
import com.wangzhenfei.cocos2dgame.R;
import com.wangzhenfei.cocos2dgame.config.RequestCode;
import com.wangzhenfei.cocos2dgame.https.RestTemplate;
import com.wangzhenfei.cocos2dgame.model.RegisterInfo;
import com.wangzhenfei.cocos2dgame.model.SaveUserInfo;
import com.wangzhenfei.cocos2dgame.socket.MsgData;
import com.wangzhenfei.cocos2dgame.socket.MySocket;
import com.wangzhenfei.cocos2dgame.tool.AppDeviceInfo;
import com.wangzhenfei.cocos2dgame.tool.ScreenUtils;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by wangzhenfei on 2016/11/25.
 */
public class RegisterDialog extends Dialog {
    public final int REQUEST_CODE_ADD_PHOTOS = 1024; // 添加三张照片
    @Bind(R.id.im_avatar)
    ImageView imAvatar;
    @Bind(R.id.et_nickname)
    EditText etNickname;
    @Bind(R.id.pb)
    ProgressBar pb;

    private Activity mActivity;
    private String localAvatarPath;
    private String avatarUrl;

    public RegisterDialog(Activity context) {
        super(context);
        this.mActivity = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = this.getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_register);
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.width = ScreenUtils.getScreenWidth(mActivity);
        attributes.gravity = Gravity.CENTER;
        ButterKnife.bind(this);
        //设置点击其他地方不能取消 和点返回键不可取消
        this.setCanceledOnTouchOutside(false);
        this.setCancelable(false);
        initImagePicker();//初始化图片选择器
        initDatas();
    }

    private void initDatas() {
        pb.setVisibility(View.INVISIBLE);
    }

    private void initImagePicker() {
        ImagePicker imagePicker = ImagePicker.getInstance();
        imagePicker.setCrop(true);                            //是否需要剪切
        imagePicker.setStyle(CropImageView.Style.RECTANGLE);  //矩形剪切
        imagePicker.setShowCamera(true); // 显示摄像头
        // 宽高默认280
        int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 280, getContext().getResources().getDisplayMetrics());
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 280, getContext().getResources().getDisplayMetrics());
        imagePicker.setFocusWidth(width);
        imagePicker.setFocusHeight(height);
        // 图片保存的宽高800
        imagePicker.setOutPutX(Integer.valueOf(800));
        imagePicker.setOutPutY(Integer.valueOf(800));
        imagePicker.setImageLoader(new ImagePickerImageLoader()); //图片加载器
        imagePicker.setMultiMode(false);                          //单选
    }

    /*
        * 获取截图结果
        * */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_ADD_PHOTOS:
                //设置图片
                if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
                    if (data != null && requestCode == REQUEST_CODE_ADD_PHOTOS) {
                        ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra
                                (ImagePicker.EXTRA_RESULT_ITEMS);
                        if (images.size() <= 0) {
                            return;
                        }
                        avatarUrl = null;
                        localAvatarPath = images.get(0).path;
                        //设置预览图片
                        new ImagePickerImageLoader().displayImage(mActivity, localAvatarPath,imAvatar,0,0);
                    }
                }
        }
    }


    @OnClick({R.id.im_avatar, R.id.btn_register})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.im_avatar:
                Intent intent = new Intent();
                intent.setClass(mActivity, ImageGridActivity.class);
                mActivity.startActivityForResult(intent, REQUEST_CODE_ADD_PHOTOS);
                break;
            case R.id.btn_register:
                commitResource();
                break;
        }
    }

    private void commitResource() {
        if(TextUtils.isEmpty(localAvatarPath)){
            Toast.makeText(mActivity, "请上传头像", Toast.LENGTH_SHORT).show();
            return;
        }
        String nName = etNickname.getText().toString().trim();
        if(nName.length() < 2){
            Toast.makeText(mActivity, "请输入正确的用户名", Toast.LENGTH_SHORT).show();
        }

        if(TextUtils.isEmpty(avatarUrl)){
            RestTemplate restTemplate = new RestTemplate(mActivity);
            RequestParams params = new RequestParams();
            try {
                params.put("imageFile", new File(localAvatarPath));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            params.put("type", "game");
            restTemplate.post(RequestCode.UP_LOAD_PATH, params, new JsonHttpResponseHandler("UTF-8") {

                @Override
                public void onSuccess(int statusCode, Header[] headers,
                                      JSONObject response) {
                    pb.setVisibility(View.INVISIBLE);
                    try {
                        int code = Integer.valueOf(response.getString("code"));
                        if(code == 0){ // 成功
                            response = response.getJSONObject("data");
                            String photoUrl = response.getString("photoUrl");
                            if(!TextUtils.isEmpty(photoUrl)){
                                avatarUrl = photoUrl;
                                register();
                            }
                        }else {
                            Toast.makeText(mActivity, response.getString("message"), Toast.LENGTH_LONG).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }catch (NumberFormatException e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onProgress(long bytesWritten, long totalSize) {
                    super.onProgress(bytesWritten, totalSize);
                    if(pb.getVisibility() != View.VISIBLE){
                        pb.setVisibility(View.VISIBLE);
                    }
                    pb.setProgress((int)(bytesWritten * 100 / totalSize));
                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                                      String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    pb.setVisibility(View.INVISIBLE);
                }
            });
        }else {
            register();
        }

    }

    public void register(){
        MsgData<RegisterInfo> msgData = new MsgData<RegisterInfo>();
        msgData.setCode(RequestCode.REGISTER);
        msgData.setData(new RegisterInfo(etNickname.getText().toString().trim(), avatarUrl, AppDeviceInfo.getIpAddress()));
        MySocket.getInstance().setMessage(msgData);
    }
}
