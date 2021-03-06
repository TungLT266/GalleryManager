package org.horaapps.leafpic.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;

import org.horaapps.leafpic.data.Media;
import org.horaapps.liz.ThemeHelper;
import org.horaapps.liz.ThemedFragment;

public abstract class BaseMediaFragment extends ThemedFragment {

    private static final String ARGS_MEDIA = "args_media";

    protected Media media;
    private MediaTapListener mediaTapListener;

    @NonNull
    protected static <T extends BaseMediaFragment> T newInstance(@NonNull T mediaFragment,
                                                                 @NonNull Media media) {

        Bundle args = new Bundle();
        args.putParcelable(ARGS_MEDIA, media);
        mediaFragment.setArguments(args);
        return mediaFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MediaTapListener) mediaTapListener = (MediaTapListener) context;
    }

    private void fetchArgs() {
        Bundle args = getArguments();
        if (args == null) throw new RuntimeException("Must pass arguments to Media Fragments!");
        media = getArguments().getParcelable(ARGS_MEDIA);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fetchArgs();
    }

    @Override
    public void refreshTheme(ThemeHelper themeHelper) {

    }

    protected void setTapListener(@NonNull View view) {
        view.setOnClickListener(v -> onTapped());
    }

    private void onTapped() {
        mediaTapListener.onViewTapped();
    }


    public interface MediaTapListener {


        void onViewTapped();
    }
}
