package com.mateu.cats.ui.activity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.mateu.cats.MyApplication;
import com.mateu.cats.R;
import com.mateu.cats.ui.BaseActivity;
import com.mateu.cats.ui.dialog.SelectPictureBottomDialog;
import com.mateu.cats.utils.MinioUtil;
import com.mateu.cats.utils.PhotoUtils;

import java.io.File;

/**
 * @author Alian Lee
 */
public class SplashActivity extends BaseActivity {

    @Override
    protected boolean isFullScreen() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // 获取访问链接
//        MinioUtil.getInstance().getMinioKeyUrl("mate_u_QLXJC0fFM6K1649761588598.png");

        // 选择照片 上传
//        showSelectPictureDialog();
    }


    /**
     * 选择图片的 dialog
     */
    private void showSelectPictureDialog() {
        SelectPictureBottomDialog.Builder builder = new SelectPictureBottomDialog.Builder();
        builder.setOnSelectPictureListener(new SelectPictureBottomDialog.OnSelectPictureListener() {
            @Override
            public void onSelectPicture(Uri uri) {
                File file = new File(uri.getPath());
                if (!file.exists()) {
                    file = new File(getRealPathFromUri(uri));
                }
                MinioUtil.getInstance().minioUploadFileStart(file, new MinioUtil.OnMinioListener() {
                    @Override
                    public void OnMinioCallback(float speed, String key, String url, boolean isEnd) {
                        Log.e("TAG", "speed: " + speed + " - key: " + key + " - url: " + url + " - isEnd: " + isEnd);
                    }
                });
            }
        });
        SelectPictureBottomDialog dialog = builder.build();
        dialog.setType(PhotoUtils.NO_CROP);
        dialog.show(getSupportFragmentManager(), "select_picture_dialog");
    }

    /**
     * 获取本地文件真实 uri
     *
     * @param contentUri
     * @return
     */
    public String getRealPathFromUri(Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


}
