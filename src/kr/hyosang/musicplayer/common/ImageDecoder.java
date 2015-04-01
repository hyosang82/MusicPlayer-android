package kr.hyosang.musicplayer.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;

import kr.hyosang.android.common.Logger;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.LruCache;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

public class ImageDecoder extends Thread {
    private static final int QUEUE_SIZE = 100;
    
    private static final int MSG_UPDATE_IMAGEVIEW = 0x01;
    
    private static ImageDecoder mInstance = null;
    
    private ArrayBlockingQueue<DecoderHolder> mBaseQueue;
    private DownloaderThread mDownloader;
    private DecoderThread mDecoder;
    private LruCache<String, WeakReference<Bitmap>> mBitmapCache;
    private String mCacheRoot = Environment.getExternalStorageDirectory().getAbsolutePath();
    
    
    private ImageDecoder() {
        //block default constructor
    }
    
    public static ImageDecoder createInstance(String cacheDir) {
        if(mInstance != null) {
            //release
        }
        
        mInstance = new ImageDecoder();
        mInstance.init(cacheDir);
        mInstance.start();
        
        return mInstance;
        
    }
    
    public static ImageDecoder getInstance() {
        return mInstance;
    }
    
    private void init(String cacheRoot) {
        mBaseQueue = new ArrayBlockingQueue<DecoderHolder>(QUEUE_SIZE);
        mCacheRoot = cacheRoot;
        
        mBitmapCache = new LruCache<String, WeakReference<Bitmap>>(100);
        
        Logger.d("ImageDecoder cache root = " + cacheRoot);
        
        //start sub threads
        mDownloader = new DownloaderThread();
        mDecoder = new DecoderThread();
        
        mDownloader.start();
        mDecoder.start();
        
    }
    
    
    @Override
    public void run() {
        while(true) {
            DecoderHolder data = null;
            boolean bAdded = false;
            
            try {
                data = mBaseQueue.take();
                
                if(data != null) {
                    if(data.localPath != null) {
                        if((new File(data.localPath)).exists()) {
                            //저장된 파일 존재함
                            mDecoder.add(data);
                            bAdded = true;
                        }
                    }
                    
                    if(!bAdded) {
                        //다운로드 주소 있는지 체크
                        if(data.imageUrl != null) {
                            mDownloader.add(data);
                            bAdded = true;
                        }
                    }
                    
                    
                    if(!bAdded) {
                        Logger.w("Queue add failed. No local/download url");
                    }
                }
                
            }catch(InterruptedException e) {
                Logger.w(e);
            }
        }
    }
    
    public void add(String resource, ImageView iv) {
        if((resource != null) && (iv != null)) {
            DecoderHolder item = new DecoderHolder();
            
            if(resource.startsWith("http://") || resource.startsWith("https://")) {
                item.imageUrl = resource;
                
                //로컬 저장 파일명 결정
                item.localPath = String.format("%s/%s", mCacheRoot, getKeyString(item));
            }else {
                item.localPath = resource;
            }
            item.targetView = iv;
            
            try {
                mBaseQueue.add(item);
            }catch(IllegalStateException e) {
                //queue full
                Logger.e("Default queue full!");
            }
        }
    }
    
    private String getKeyString(DecoderHolder item) {
        String baseStr = null;
        
        if(item.imageUrl != null && !item.imageUrl.isEmpty()) {
            baseStr = item.imageUrl;
        }else if(item.localPath != null && !item.localPath.isEmpty()) {
            baseStr = item.localPath;
        }else {
            return null;
        }
        
        baseStr = baseStr.replaceAll("[^0-9A-Za-z]", "");
        
        if(baseStr.length() > 20) {
            baseStr = baseStr.substring(baseStr.length() - 20);
        }
        
        return baseStr;
    }
    
    private String getKeyString(String mainKey, ImageView iv) {
        LayoutParams lp = iv.getLayoutParams();
        return String.format("%s_%d_%d", mainKey, lp.width, lp.height);
    }
    
    private class DownloaderThread extends Thread {
        private ArrayBlockingQueue<DecoderHolder> mDownloadQueue;
        
        public DownloaderThread() {
            mDownloadQueue = new ArrayBlockingQueue<DecoderHolder>(QUEUE_SIZE);
        }
        
        public void add(DecoderHolder item) {
            try {
                mDownloadQueue.add(item);
            }catch(IllegalStateException e) {
                //queue full
                Logger.e("Downloader queue full!");
            }
        }
        
        @Override
        public void run() {
            while(true) {
                DecoderHolder data = null;
                try {
                    data = mDownloadQueue.take();
                    
                    try {
                        URL url = new URL(data.imageUrl);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        
                        conn.setDoInput(true);
                        conn.setRequestMethod("GET");
                        
                        conn.connect();
                        
                        InputStream is = conn.getInputStream();
                        
                        FileOutputStream fos = new FileOutputStream(data.localPath);
                        
                        byte [] buf = new byte[1024];
                        int nRead;
                        
                        while((nRead = is.read(buf)) > 0) {
                            fos.write(buf, 0, nRead);
                        }
                        
                        fos.close();
                        
                        is.close();
                        
                        conn.disconnect();
                        
                        mDecoder.add(data);
                    }catch(MalformedURLException e) {
                        Logger.w(e);
                    }catch(IOException e) {
                        Logger.w(e);
                    }
                }catch(InterruptedException e) {
                    Logger.w(e);
                }
            }
        }
    }
    
    private class DecoderThread extends Thread {
        private ArrayBlockingQueue<DecoderHolder> mDecoderQueue;
        
        public DecoderThread() {
            mDecoderQueue = new ArrayBlockingQueue<DecoderHolder>(QUEUE_SIZE);
        }
        
        public void add(DecoderHolder item) {
            try {
                mDecoderQueue.add(item);
            }catch(IllegalStateException e) {
                //queue full
                Logger.e("Decoder queue full");
            }
        }
        
        @Override
        public void run() {
            while(true) {
                DecoderHolder item = null;
                
                try {
                    item = mDecoderQueue.take();
                    
                    //캐시 확인
                    String key = getKeyString(getKeyString(item), item.targetView);
                    WeakReference<Bitmap> obj = mBitmapCache.get(key);
                    
                    if(obj != null && obj.get() != null) {
                        item.bitmap = obj.get();
                        
                        Message.obtain(mHandler, MSG_UPDATE_IMAGEVIEW, item).sendToTarget();
                    }else {
                        //캐시 없음
                        if(item.localPath != null) {
                            File f = new File(item.localPath);
                            
                            if(f.exists()) {
                                //타겟 크기 구함
                                LayoutParams lp = item.targetView.getLayoutParams();
                                
                                Bitmap b = BitmapFactory.decodeFile(item.localPath);
                                if(b != null) {
                                    item.bitmap = Bitmap.createScaledBitmap(b, lp.width, lp.height, false);
                                    
                                    if(!b.equals(item.bitmap)) {
                                        b.recycle();
                                    }
                                    
                                    //캐시 저장
                                    WeakReference<Bitmap> ref = new WeakReference<Bitmap>(item.bitmap);
                                    mBitmapCache.put(key, ref);
                                    
                                    Message.obtain(mHandler, MSG_UPDATE_IMAGEVIEW, item).sendToTarget();
                                }
                            }
                        }
                    }
                    
                }catch(InterruptedException e) {
                    Logger.w(e);
                } 
            }
        }
    }
    
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
            case MSG_UPDATE_IMAGEVIEW:
            {
                DecoderHolder data = (DecoderHolder) msg.obj;
                
                if(data.bitmap != null && data.targetView != null) {
                    data.targetView.setImageBitmap(data.bitmap);
                }
            }
            break;
            }
        }
    };
    
   
    
    public static class DecoderHolder {
        public String imageUrl = null;
        public String localPath = null;
        public Bitmap bitmap = null;
        public ImageView targetView = null;
    }
}
