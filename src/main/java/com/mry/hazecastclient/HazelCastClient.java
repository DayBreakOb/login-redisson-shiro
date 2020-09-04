package com.mry.hazecastclient;

import com.hazelcast.license.domain.License;
import com.hazelcast.license.extractor.LicenseExtractorV4;

public class HazelCastClient {

	
	public static void main(String[] args) {
		License result = LicenseExtractorV4.extractLicense("dsx4MZQvo2tqegGLpWhCDXnPYzciBR3murF5SA7KIObaw1Vf6JNyjEl0UTkH");
		System.out.println(result.toString());
	}
}
