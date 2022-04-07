package com.example.sharebyemail;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    ImageView iv;
    String fileName;

    String saveDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()+ "/shareByEmail"; // 파일 경로

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        iv = (ImageView) findViewById(R.id.iv);

        // storage 접근 권한 확인
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "외부 저장소 사용을 위해 읽기/쓰기 필요", Toast.LENGTH_SHORT).show();
                }

                requestPermissions(new String[]
                        {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
            }
        }
    }

    public void onClickShareByEmail(View view) {
        Intent ShareEmail = new Intent(Intent.ACTION_SEND); // Intent.ACTION_SEND 인텐트 생성
        ShareEmail.setType("plain/text"); // setType 설정 하는 거 : 이메일로 보낼지 뭐 할지 선택하는 거

        ShareEmail.putExtra(Intent.EXTRA_SUBJECT, "사진 전송");
        ShareEmail.putExtra(Intent.EXTRA_TEXT, "이미지뷰의 사진 이메일로 전송하는 예제");

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss"); // 올해년도몇월며칠_몇시몇분몇초 형식으로 포맷하겠다.
        saveImageToSendFile(((BitmapDrawable) iv.getDrawable()).getBitmap(), simpleDateFormat.format(new Date()));

        Log.i("jeongmin", "첨부파일 경로 : " + saveDir);

        ShareEmail.putExtra(Intent.EXTRA_STREAM, Uri.parse(saveDir)); // 왜 파일을 첨부할 수 없다고 뜨나요?
//        iv.setImageURI((Uri) getIntent().getExtras().get(Intent.EXTRA_STREAM));

        startActivity(ShareEmail);

        // 이메일 공유 할 때 권한 주기
        ShareEmail.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // 쓰기 권한
        ShareEmail.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION); // 읽기 권한
    }

    public boolean saveImageToSendFile(Bitmap bitmap, String saveImageName) {

//        String saveDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()+ "/shareByEmail";

        Log.i("jeongmin", "경로 : " + saveDir);

        File file = new File(saveDir);
        if (!file.exists()) {
            file.mkdir();
            Log.i("jeongmin", "폴더가 없습니다.");
        }
        Log.i("jeongmin", "폴더가 있습니다.");

        fileName = saveImageName + ".png";
        File tempFile = new File(saveDir, fileName);
        FileOutputStream output = null;

        Log.i("jeongmin", "try-catch 문 진입 전");
        try {
            if (tempFile.createNewFile()) {
                output = new FileOutputStream(tempFile);
                // 이미지 줄이기
                Bitmap newBitmap = bitmap.createScaledBitmap(bitmap, 200, 200, true);
                // 이미지 압축. 압축된 파일은 output stream 에 저장. 2번째 인자는 압축률인데 100으로 해도 많이 깨짐..
                newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
                Log.i("jeongmin", "if 문 속 : 저장은 함");
                Toast.makeText(getApplicationContext(), "이미지를 저장했습니다.\n이미지 이름 : " + fileName, Toast.LENGTH_SHORT).show();
                Log.i("jeongmin", "파일 이름 : " + fileName);
                // 이미지 스캐닝 해서 갤러리에서 보이게 해 주는 코드
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(tempFile)));
            } else {
                Toast.makeText(getApplicationContext(), "같은 이름의 파일이 존재합니다.", Toast.LENGTH_SHORT).show();
                Log.i("jeongmin", "else 문 속 : 같은 이름의 파일 존재:"+fileName);

                return false;
            }
            Log.i("jeongmin", "try 문 안의 if 문도 else 문도 진입하지 않음");
        } catch (FileNotFoundException e) {
            Log.i("jeongmin", "FileNotFoundException 에러 : 파일을 찾을 수 없음" + e);
            return false;

        } catch (IOException e) {
            Log.i("jeongmin", "IOException 에러 : " + e);
            e.printStackTrace();
            return false;

        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Log.i("jeongmin", "try-catch 문 후의 끝");
        return true;
    }
}