package com.vcall.xr.video.domain;

import lombok.Getter;

@Getter
public enum VideoType {
    MONOSCOPIC("Monoscopic 360", "Single viewpoint equirectangular projection"),
    STEREOSCOPIC("Stereoscopic 360", "Side-by-side or top-bottom 3D projection");

    private final String displayName;
    private final String description;

    VideoType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}
