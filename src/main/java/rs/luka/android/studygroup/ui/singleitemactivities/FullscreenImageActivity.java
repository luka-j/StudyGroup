package rs.luka.android.studygroup.ui.singleitemactivities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import rs.luka.android.studygroup.R;

/**
 * Created by luka on 11.9.15.
 */
public class FullscreenImageActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGE_PATH = "imagePath";
    private SubsamplingScaleImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_image);

        imageView = (SubsamplingScaleImageView) findViewById(R.id.fullscreen_image);
        imageView.setImage(ImageSource.uri(getIntent().getStringExtra(EXTRA_IMAGE_PATH)));
        imageView.setOrientation(SubsamplingScaleImageView.ORIENTATION_USE_EXIF);
    }
}
