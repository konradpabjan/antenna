/*
 * Copyright (c) Bosch Software Innovations GmbH 2019.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.workflow.generator;

import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.*;
import org.eclipse.sw360.antenna.model.artifact.facts.java.MavenCoordinates;
import org.eclipse.sw360.antenna.model.xml.generated.License;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseInformation;
import org.eclipse.sw360.antenna.sw360.model.artifact.facts.ArtifactChangeStatus;
import org.eclipse.sw360.antenna.sw360.model.artifact.facts.ArtifactClearingState;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360Component;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.utils.SW360ReleaseAdapterUtils;
import org.eclipse.sw360.antenna.testing.AntennaTestWithMockedContext;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class SW360UpdaterTest extends AntennaTestWithMockedContext {

    private String sourceUrl = "source_url.com";
    private String releaseTagUrl = "release_tag.com";
    private String swhUrl = "software_heritage.com";

    @Test
    public void artifactIsMappedToSw360ReleaseCorrectly() {
        Artifact artifact = mkArtifact("test1");
        SW360Release release = new SW360Release();
        SW360Component sw360Component = new SW360Component();
        sw360Component.setName("artifactIdtest1");
        SW360ReleaseAdapterUtils.prepareRelease(release, sw360Component, Collections.EMPTY_SET, artifact);

        assertThat(release.getClearingState()).isEqualTo(ArtifactClearingState.ClearingState.PROJECT_APPROVED.toString());
        assertThat(release.getChangeStatus()).isEqualTo(ArtifactChangeStatus.ChangeStatus.CHANGED.toString());

        assertThat(release.getDownloadurl()).isEqualTo(sourceUrl);
        assertThat(release.getReleaseTagUrl()).isEqualTo(releaseTagUrl);
        assertThat(release.getSoftwareHeritageUrl()).isEqualTo(swhUrl);

        assertThat(release.getCoordinates()).containsKeys("mvn");
        assertThat(release.getCoordinates()).hasSize(1);
        assertThat(release.getCoordinates()).containsValue("org.group.id:artifactId(test1):1.2.3");

        assertThat(release.getHashes()).hasSize(1);
        assertThat(release.getHashes()).
                isEqualTo(artifact.askForAll(ArtifactFilename.class)
                        .stream()
                        .map(ArtifactFilename::getHash)
                        .collect(Collectors.toSet()));

        assertThat(release.getDeclaredLicense()).contains("license name (test1)");
        assertThat(release.getObservedLicense()).contains("license name (test1)");
    }

    private Artifact mkArtifact(String name) {
        // License information
        LicenseInformation licenseInformation = new LicenseInformation() {
            @Override
            public String evaluate() {
                return "evaluated license (" + name + ")";
            }

            @Override
            public String evaluateLong() {
                return "long evaluated license (" + name + ")";
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public List<License> getLicenses() {
                License license = new License();
                license.setLongName("license long name (" + name + ")");
                license.setText("license text (" + name + ")");
                license.setName("license name (" + name + ")");
                return Collections.singletonList(license);
            }

            @Override
            public String getLinkStr() {
                return "https://link.to.license" + name + ".invalid";
            }

            @Override
            public String getHTMLLinkStr() {
                return getLinkStr();
            }
        };
        Artifact artifact = new Artifact("JSON");
        artifact.setProprietary(false);
        artifact.addFact(new MavenCoordinates("artifactId(" + name + ")", "org.group.id", "1.2.3"));
        artifact.addFact(new DeclaredLicenseInformation(licenseInformation));
        artifact.addFact(new ObservedLicenseInformation(licenseInformation));
        artifact.addFact(new ArtifactSourceUrl(sourceUrl));
        artifact.addFact(new ArtifactReleaseTagURL(releaseTagUrl));
        artifact.addFact(new ArtifactSoftwareHeritageURL(swhUrl));
        artifact.addFact(new ArtifactFilename("test-file.jar", ("12345678" + name)));
        artifact.addFact(new ArtifactClearingState(ArtifactClearingState.ClearingState.valueOf("PROJECT_APPROVED")));
        artifact.addFact(new ArtifactChangeStatus(ArtifactChangeStatus.ChangeStatus.valueOf("CHANGED")));

        return artifact;
    }
}
