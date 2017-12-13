package team.itis.ivm.ui.activities;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.miguelgaeta.media_picker.MediaPicker;
import com.miguelgaeta.media_picker.MediaPickerRequest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import team.itis.ivm.helpers.FFmpegHelper;
import team.itis.ivm.R;
import team.itis.ivm.ui.fragments.ProcessFragment;
import team.itis.ivm.ui.fragments.ProjectsFragment;
import team.itis.ivm.ui.fragments.SoundsFragment;
import team.itis.ivm.ui.fragments.TextsFragment;
import team.itis.ivm.ui.fragments.ViewsFragment;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    static final int OPEN_MEDIA_PICKER = 1;
    Fragment projectFragment, viewsFragment, textsFragment, soundsFragment;
    ProcessFragment processFragment;
    ViewPager mViewPager;
    BottomNavigationView navigation;
    ArrayList<String> selectionResult = new ArrayList<>();
    int menuPages[] = new int[]{R.id.navigation_project, R.id.navigation_views, R.id.navigation_texts, R.id.navigation_sounds, R.id.navigation_process};

    public ArrayList<String> getSelectionResult() {
        return selectionResult;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(this);
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mSectionsPagerAdapter.add(projectFragment = new ProjectsFragment());
        mSectionsPagerAdapter.add(viewsFragment = new ViewsFragment());
        mSectionsPagerAdapter.add(textsFragment = new TextsFragment());
        mSectionsPagerAdapter.add(soundsFragment = new SoundsFragment());
        mSectionsPagerAdapter.add(processFragment = new ProcessFragment());

        mViewPager = findViewById(R.id.main_frame);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                System.out.println();
            }

            @Override
            public void onPageSelected(int position) {
                navigation.setSelectedItemId(menuPages[position]);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                System.out.println();
            }
        });
        mViewPager.setAdapter(mSectionsPagerAdapter);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            FFmpegHelper.getInstance().initFFmpeg(this);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        selectionResult.add(getPath(this, data.getData()));
        MediaPicker.handleActivityResult(this, requestCode, resultCode, data, new MediaPicker.OnResult() {

            @Override
            public void onError(IOException e) {

                Log.e("MediaPicker", "Got file error.", e);
            }

            @Override
            public void onSuccess(File mediaFile, MediaPickerRequest request) {

                Log.e("MediaPicker", "Got file result: " + mediaFile + " for code: " + request);
                selectionResult.add(mediaFile.getAbsolutePath());
            }

            @Override
            public void onCancelled() {

                Log.e("MediaPicker", "Got cancelled event.");
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                FFmpegHelper.getInstance().initFFmpeg(this);
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navigation_project:
                mViewPager.setCurrentItem(0, true);
                getSupportActionBar().setTitle(R.string.title_project);
                break;
            case R.id.navigation_views:
                mViewPager.setCurrentItem(1, true);
                getSupportActionBar().setTitle(R.string.title_views);
                break;
            case R.id.navigation_texts:
                mViewPager.setCurrentItem(2, true);
                getSupportActionBar().setTitle(R.string.title_text);
                break;
            case R.id.navigation_sounds:
                mViewPager.setCurrentItem(3, true);
                getSupportActionBar().setTitle(R.string.title_music);
                break;
            case R.id.navigation_process:
                mViewPager.setCurrentItem(4, true);
                getSupportActionBar().setTitle(R.string.title_process);
                break;
            default:
                mViewPager.setCurrentItem(0, true);
                break;
        }

        return true;
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        ArrayList<Fragment> items = new ArrayList<>();

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            navigation.setSelectedItemId(position);
            return items.get(position);
        }

        @Override
        public int getCount() {
            return items.size();
        }

        void add(Fragment fragment) {
            items.add(fragment);
        }
    }

    public static String getPath(final Context context, final Uri uri) {

        // DocumentProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, uri)) {

            if (isExternalStorageDocument(uri)) {// ExternalStorageProvider
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                String storageDefinition;


                if ("primary".equalsIgnoreCase(type)) {

                    return Environment.getExternalStorageDirectory() + "/" + split[1];

                } else {

                    if (Environment.isExternalStorageRemovable()) {
                        storageDefinition = "EXTERNAL_STORAGE";

                    } else {
                        storageDefinition = "SECONDARY_STORAGE";
                    }

                    return System.getenv(storageDefinition) + "/" + split[1];
                }

            } else if (isDownloadsDocument(uri)) {// DownloadsProvider

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);

            } else if (isMediaDocument(uri)) {// MediaProvider
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }

        } else if ("content".equalsIgnoreCase(uri.getScheme())) {// MediaStore (and general)

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);

        } else if ("file".equalsIgnoreCase(uri.getScheme())) {// File
            return uri.getPath();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }


    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
}
