
package org.horaapps.leafpic.views.videoplayer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.RendererCapabilities;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.FixedTrackSelection;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector.MappedTrackInfo;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector.SelectionOverride;
import com.google.android.exoplayer2.trackselection.RandomTrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.util.MimeTypes;

import org.horaapps.leafpic.R;
import org.horaapps.liz.ThemeHelper;

import java.util.Arrays;
import java.util.Locale;


 public final class TrackSelectionHelper implements View.OnClickListener,
        DialogInterface.OnClickListener {

  private static final TrackSelection.Factory FIXED_FACTORY = new FixedTrackSelection.Factory();
  private static final TrackSelection.Factory RANDOM_FACTORY = new RandomTrackSelection.Factory();

  private final MappingTrackSelector selector;
  private final TrackSelection.Factory adaptiveVideoTrackSelectionFactory;

  private MappedTrackInfo trackInfo;
  private int rendererIndex;
  private TrackGroupArray trackGroups;
  private boolean[] trackGroupsAdaptive;
  private boolean isDisabled;
  private SelectionOverride override;
  private CheckedTextView[][] trackViews;
  private ThemeHelper themeHelper;


  public TrackSelectionHelper(MappingTrackSelector selector, TrackSelection.Factory adaptiveVideoTrackSelectionFactory, ThemeHelper themeHelper) {
    this.selector = selector;
    this.adaptiveVideoTrackSelectionFactory = adaptiveVideoTrackSelectionFactory;
    this.themeHelper = themeHelper;
  }




  public void showSelectionDialog(Activity activity, CharSequence title, MappedTrackInfo trackInfo,
                                  int rendererIndex) {
    this.trackInfo = trackInfo;
    this.rendererIndex = rendererIndex;

    trackGroups = trackInfo.getTrackGroups(rendererIndex);
    trackGroupsAdaptive = new boolean[trackGroups.length];
    for (int i = 0; i < trackGroups.length; i++) {
      trackGroupsAdaptive[i] = adaptiveVideoTrackSelectionFactory != null
              && trackInfo.getAdaptiveSupport(rendererIndex, i, false)
              != RendererCapabilities.ADAPTIVE_NOT_SUPPORTED
              && trackGroups.get(i).length > 1;
    }
    isDisabled = selector.getRendererDisabled(rendererIndex);
    override = selector.getSelectionOverride(rendererIndex, trackGroups);

    AlertDialog.Builder builder = new AlertDialog.Builder(activity, themeHelper.getDialogStyle());
    builder.setTitle(title)
            .setView(buildView(LayoutInflater.from(builder.getContext())))
            .setPositiveButton(android.R.string.ok, this)
            .setNegativeButton(android.R.string.cancel, null)
            .create()
            .show();
  }

  @SuppressLint("InflateParams")
  private View buildView(LayoutInflater inflater) {
    View view = inflater.inflate(R.layout.track_selection_dialog, null);
    ViewGroup root = view.findViewById(R.id.root);

    trackViews = new CheckedTextView[trackGroups.length][];
    for (int groupIndex = 0; groupIndex < trackGroups.length; groupIndex++) {
      TrackGroup group = trackGroups.get(groupIndex);
      boolean groupIsAdaptive = trackGroupsAdaptive[groupIndex];
      trackViews[groupIndex] = new CheckedTextView[group.length];
      for (int trackIndex = 0; trackIndex < group.length; trackIndex++) {
        if (trackIndex == 0) {
          root.addView(inflater.inflate(R.layout.list_divider, root, false));
        }
        int trackViewLayoutId = groupIsAdaptive ? android.R.layout.simple_list_item_multiple_choice
                : android.R.layout.simple_list_item_single_choice;
        CheckedTextView trackView = (CheckedTextView) inflater.inflate(
                trackViewLayoutId, root, false);
        trackView.setText(buildTrackName(group.getFormat(trackIndex)));
        if (trackInfo.getTrackFormatSupport(rendererIndex, groupIndex, trackIndex)
                == RendererCapabilities.FORMAT_HANDLED) {
          trackView.setFocusable(true);
          trackView.setTag(Pair.create(groupIndex, trackIndex));
          trackView.setOnClickListener(this);
        } else {
          trackView.setFocusable(false);
          trackView.setEnabled(false);
        }
        trackViews[groupIndex][trackIndex] = trackView;
        root.addView(trackView);
      }
    }

    updateViews();
    return view;
  }

  private void updateViews() {

    for (int i = 0; i < trackViews.length; i++) {
      for (int j = 0; j < trackViews[i].length; j++) {
        trackViews[i][j].setChecked(override != null && override.groupIndex == i
                && override.containsTrack(j));
      }
    }
  }



  @Override
  public void onClick(DialogInterface dialog, int which) {
    selector.setRendererDisabled(rendererIndex, isDisabled);
    if (override != null) {
      selector.setSelectionOverride(rendererIndex, trackGroups, override);
    } else {
      selector.clearSelectionOverrides(rendererIndex);
    }
  }



  @Override
  public void onClick(View view) {

    isDisabled = false;

    Pair<Integer, Integer> tag = (Pair<Integer, Integer>) view.getTag();
    int groupIndex = tag.first;
    int trackIndex = tag.second;
    if (!trackGroupsAdaptive[groupIndex] || override == null
            || override.groupIndex != groupIndex) {
      override = new SelectionOverride(FIXED_FACTORY, groupIndex, trackIndex);
    } else {

      boolean isEnabled = ((CheckedTextView) view).isChecked();
      int overrideLength = override.length;
      if (isEnabled) {

        if (overrideLength == 1) {

          override = null;
          isDisabled = true;
        } else {
          setOverride(groupIndex, getTracksRemoving(override, trackIndex), false);
        }
      } else {

        setOverride(groupIndex, getTracksAdding(override, trackIndex), false);
      }
    }

    updateViews();
  }

  private void setOverride(int group, int[] tracks, boolean enableRandomAdaptation) {
    TrackSelection.Factory factory = tracks.length == 1 ? FIXED_FACTORY
            : (enableRandomAdaptation ? RANDOM_FACTORY : adaptiveVideoTrackSelectionFactory);
    override = new SelectionOverride(factory, group, tracks);
  }



  private static int[] getTracksAdding(SelectionOverride override, int addedTrack) {
    int[] tracks = override.tracks;
    tracks = Arrays.copyOf(tracks, tracks.length + 1);
    tracks[tracks.length - 1] = addedTrack;
    return tracks;
  }

  private static int[] getTracksRemoving(SelectionOverride override, int removedTrack) {
    int[] tracks = new int[override.length - 1];
    int trackCount = 0;
    for (int i = 0; i < tracks.length + 1; i++) {
      int track = override.tracks[i];
      if (track != removedTrack) {
        tracks[trackCount++] = track;
      }
    }
    return tracks;
  }



  private static String buildTrackName(Format format) {
    String trackName;
    if (MimeTypes.isVideo(format.sampleMimeType)) {
      trackName = joinWithSeparator(joinWithSeparator(buildResolutionString(format),
              buildBitrateString(format)), buildTrackIdString(format));
    } else if (MimeTypes.isAudio(format.sampleMimeType)) {
      trackName = joinWithSeparator(joinWithSeparator(joinWithSeparator(buildLanguageString(format),
              buildAudioPropertyString(format)), buildBitrateString(format)),
              buildTrackIdString(format));
    } else {
      trackName = joinWithSeparator(joinWithSeparator(buildLanguageString(format),
              buildBitrateString(format)), buildTrackIdString(format));
    }
    return trackName.length() == 0 ? "unknown" : trackName;
  }

  private static String buildResolutionString(Format format) {
    return format.width == Format.NO_VALUE || format.height == Format.NO_VALUE
            ? "" : format.width + "x" + format.height;
  }

  private static String buildAudioPropertyString(Format format) {
    return format.channelCount == Format.NO_VALUE || format.sampleRate == Format.NO_VALUE
            ? "" : format.channelCount + "ch, " + format.sampleRate + "Hz";
  }

  private static String buildLanguageString(Format format) {
    return TextUtils.isEmpty(format.language) || "und".equals(format.language) ? ""
            : format.language;
  }

  private static String buildBitrateString(Format format) {
    return format.bitrate == Format.NO_VALUE ? ""
            : String.format(Locale.US, "%.2fMbit", format.bitrate / 1000000f);
  }

  private static String joinWithSeparator(String first, String second) {
    return first.length() == 0 ? second : (second.length() == 0 ? first : first + ", " + second);
  }

  private static String buildTrackIdString(Format format) {
    return format.id == null ? "" : ("id:" + format.id);
  }
}
