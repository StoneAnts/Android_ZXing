package com.example.zxing_qrcode;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.EncodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private ImageView im1 = null;  //imageview图片
    private Button btnProduct;   //生成二维码
    private Button btnRecog;    //识别二维码
    private Button btnAddLogo;  //添加logo
    private Button btnScanner; //扫一扫

    private int w,h;        //图片宽度w,高度h
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnProduct=(Button)findViewById(R.id.btnProduct);
        btnProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText=(EditText)findViewById(R.id.editText);
                createQRcodeImage(editText.getText().toString());//自定义转换内容
            }
        });

        btnRecog=(Button)findViewById(R.id.btnRecog);
        btnRecog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                im1=(ImageView)findViewById(R.id.imageView);
                recogQRcode(im1);
            }
        });

        btnAddLogo=(Button)findViewById(R.id.btnAddLogo);
        btnAddLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                im1=(ImageView)findViewById(R.id.imageView);
                Bitmap QRbmp = ((BitmapDrawable) (im1).getDrawable()).getBitmap();   //将图片bitmap化
                //将drawable里面的图片bitmap化
                Bitmap logo = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
                im1.setImageBitmap(addLogo(QRbmp,logo));
            }
        });

        btnScanner=(Button)findViewById(R.id.btnScanner);
        btnScanner.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btnScanner:
                        //new IntentIntegrator(MainActivity.this).initiateScan();
                        IntentIntegrator intentIntegrator = new IntentIntegrator(MainActivity.this);
                        intentIntegrator.setCaptureActivity(CustomCaptureActivity.class);
                        intentIntegrator.initiateScan();
                        break;
                }
            }
        });

    }


    //转换成二维码QRcode的函数。参数为一个字符串
    public void createQRcodeImage(String url)
    {
        im1=(ImageView)findViewById(R.id.imageView);
        w=im1.getWidth();
        h=im1.getHeight();
        try
        {
            //判断URL合法性
            if (url == null || "".equals(url) || url.length() < 1)
            {
                return;
            }
            Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            //图像数据转换，使用了矩阵转换
            BitMatrix bitMatrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, w, h, hints);
            int[] pixels = new int[w * h];
            //下面这里按照二维码的算法，逐个生成二维码的图片，
            //两个for循环是图片横列扫描的结果
            for (int y = 0; y < h; y++)
            {
                for (int x = 0; x < w; x++)
                {
                    if (bitMatrix.get(x, y))
                    {
                        pixels[y * w + x] = 0xff000000;
                    }
                    else
                    {
                        pixels[y * w + x] = 0xffffffff;
                    }
                }
            }
            //生成二维码图片的格式，使用ARGB_8888
            Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
            //显示到我们的ImageView上面
            im1.setImageBitmap(bitmap);
        }
        catch (WriterException e)
        {
            e.printStackTrace();
        }
    }

    //识别二维码的函数
    public void recogQRcode(ImageView imageView){
        Bitmap QRbmp = ((BitmapDrawable) (imageView).getDrawable()).getBitmap();   //将图片bitmap化
        int width = QRbmp.getWidth();
        int height = QRbmp.getHeight();
        int[] data = new int[width * height];
        QRbmp.getPixels(data, 0, width, 0, 0, width, height);    //得到像素
        RGBLuminanceSource source = new RGBLuminanceSource(QRbmp);   //RGBLuminanceSource对象
        BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader = new QRCodeReader();
        Result re = null;
        try {
            //得到结果
            re = reader.decode(bitmap1);
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (ChecksumException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        }

        //Toast出内容
        Toast.makeText(MainActivity.this,re.getText(),Toast.LENGTH_SHORT).show();

        //利用正则表达式判断内容是否是URL，是的话则打开网页
        String regex = "(((https|http)?://)?([a-z0-9]+[.])|(www.))"
                + "\\w+[.|\\/]([a-z0-9]{0,})?[[.]([a-z0-9]{0,})]+((/[\\S&&[^,;\u4E00-\u9FA5]]+)+)?([.][a-z0-9]{0,}+|/?)";//设置正则表达式

        Pattern pat = Pattern.compile(regex.trim());//比对
        Matcher mat = pat.matcher(re.getText().trim());
        if (mat.matches()){
            Uri uri = Uri.parse(re.getText());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);//打开浏览器
            startActivity(intent);
        }

    }

    //给二维码添加图片
    //第一个参数为原二维码，第二个参数为添加的logo
    private static Bitmap addLogo(Bitmap src, Bitmap logo) {
        //如果原二维码为空，返回空
        if (src ==null ) {
            return null;
        }
        //如果logo为空，返回原二维码
        if (src ==null ||logo ==null) {
            return src;
        }

        //这里得到原二维码bitmap的数据
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        //logo的Width和Height
        int logoWidth = logo.getWidth();
        int logoHeight = logo.getHeight();

        //同样如果为空，返回空
        if (srcWidth == 0 || srcHeight == 0) {
            return null;
        }
        //同样logo大小为0，返回原二维码
        if (logoWidth == 0 || logoHeight == 0) {
            return src;
        }

        //logo大小为二维码整体大小的1/5，也可以自定义多大，越小越好
        //二维码有一定的纠错功能，中间图片越小，越容易纠错
        float scaleFactor = srcWidth * 1.0f / 8 / logoWidth;
        Bitmap bitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888);
        try {
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(src, 0, 0, null);
            canvas.scale(scaleFactor, scaleFactor, srcWidth / 2, srcHeight / 2);
            canvas.drawBitmap(logo, (srcWidth - logoWidth) / 2, (srcHeight - logoHeight) / 2,null );

            canvas.save();
            canvas.restore();
        } catch (Exception e) {
            bitmap = null;
            e.getStackTrace();
        }

        return bitmap;
    }

    //识别图片所需要的RGBLuminanceSource类
    public class RGBLuminanceSource extends LuminanceSource {

        private byte bitmapPixels[];

        protected RGBLuminanceSource(Bitmap bitmap) {
            super(bitmap.getWidth(), bitmap.getHeight());

            // 首先，要取得该图片的像素数组内容
            int[] data = new int[bitmap.getWidth() * bitmap.getHeight()];
            this.bitmapPixels = new byte[bitmap.getWidth() * bitmap.getHeight()];
            bitmap.getPixels(data, 0, getWidth(), 0, 0, getWidth(), getHeight());

            // 将int数组转换为byte数组，也就是取像素值中蓝色值部分作为辨析内容
            for (int i = 0; i < data.length; i++) {
                this.bitmapPixels[i] = (byte) data[i];
            }
        }

        @Override
        public byte[] getMatrix() {
            // 返回我们生成好的像素数据
            return bitmapPixels;
        }

        @Override
        public byte[] getRow(int y, byte[] row) {
            // 这里要得到指定行的像素数据
            System.arraycopy(bitmapPixels, y * getWidth(), row, 0, getWidth());
            return row;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null){
            if(result.getContents() != null){
                Toast.makeText(MainActivity.this,result.getContents(),Toast.LENGTH_SHORT).show();
                //利用正则表达式判断内容是否是URL，是的话则打开网页
                String regex = "(((https|http)?://)?([a-z0-9]+[.])|(www.))"
                        + "\\w+[.|\\/]([a-z0-9]{0,})?[[.]([a-z0-9]{0,})]+((/[\\S&&[^,;\u4E00-\u9FA5]]+)+)?([.][a-z0-9]{0,}+|/?)";//设置正则表达式

                Pattern pat = Pattern.compile(regex.trim());//比对
                Matcher mat = pat.matcher(result.getContents().trim());
                if (mat.matches()){
                    Uri uri = Uri.parse(result.getContents());
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);//打开浏览器
                    startActivity(intent);
                }

            }
            else{
                Toast.makeText(MainActivity.this,"Error",Toast.LENGTH_SHORT).show();
            }
        }
    }
}
