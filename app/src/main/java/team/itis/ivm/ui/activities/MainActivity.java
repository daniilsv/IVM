package team.itis.ivm.ui.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
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
import android.view.MenuItem;

import java.util.ArrayList;

import team.itis.ivm.helpers.FFmpegHelper;
import team.itis.ivm.R;
import team.itis.ivm.ui.fragments.ProcessFragment;
import team.itis.ivm.ui.fragments.ProjectsFragment;
import team.itis.ivm.ui.fragments.SoundsFragment;
import team.itis.ivm.ui.fragments.TextsFragment;
import team.itis.ivm.ui.fragments.ViewsFragment;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    Fragment projectFragment, viewsFragment, textsFragment, soundsFragment;
    ProcessFragment processFragment;
    ViewPager mViewPager;
    BottomNavigationView navigation;
    int menuPages[] = new int[]{R.id.navigation_project, R.id.navigation_views, R.id.navigation_texts, R.id.navigation_sounds, R.id.navigation_process};

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


}
