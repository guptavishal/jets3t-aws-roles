/*
 * JetS3t : Java S3 Toolkit
 * Project hosted at http://bitbucket.org/jmurty/jets3t/
 *
 * Copyright 2006-2012 James Murty
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jets3t.service.security;

import org.jets3t.service.Constants;
import org.jets3t.service.Jets3tProperties;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClient;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;

import java.util.Calendar;
import java.util.Date;


/**
 * Class to contain the temporary (session-based) Amazon Web Services (AWS) credentials fetched after a user assumes a role *
 */
public class AWSRoleSessionCredentials extends ProviderCredentials {

  protected String iamAccessKey = null;
  protected String iamSecretKey = null;
  protected String roleToBeAssumed = null;
  protected String externalId = null;

  protected String sessionToken = null;
  protected Date expirationDate = null;

  protected Jets3tProperties jets3tProperties;
  private static final String DEFAULT_SESSION_NAME = "some-session-name";

  public AWSRoleSessionCredentials(String iamAccessKey, String iamSecretKey, String roleToBeAssumed, String externalId,
                               String friendlyName, Jets3tProperties jets3tProperties){
    if(roleToBeAssumed == null){
      throw new IllegalArgumentException("roleToBeAssumed needs to be present.");
    }
    this.iamAccessKey = iamAccessKey;
    this.iamSecretKey = iamSecretKey;
    this.roleToBeAssumed = roleToBeAssumed;
    this.externalId = externalId;
    this.friendlyName = friendlyName;
    if(jets3tProperties == null){
      this.jets3tProperties = Jets3tProperties.getInstance(Constants.JETS3T_PROPERTIES_FILENAME);
    }else {
      this.jets3tProperties = jets3tProperties;
    }
    assumeRoleAndGetCredentials();
  }

  private void assumeRoleAndGetCredentials() {
    int defaultRequestedExpiryTimeInMinutes = jets3tProperties.getIntProperty("aws.session-credentials.expiry-time.to-be-requested", 60);
    com.amazonaws.auth.AWSCredentials awsCredentials = new BasicAWSCredentials(iamAccessKey, iamSecretKey);
    AWSSecurityTokenServiceClient stsClient =
            new AWSSecurityTokenServiceClient(awsCredentials);
    AssumeRoleRequest assumeRequest = new AssumeRoleRequest()
            .withRoleArn(roleToBeAssumed)
            .withDurationSeconds(defaultRequestedExpiryTimeInMinutes * 60)
            .withRoleSessionName(DEFAULT_SESSION_NAME);
    if(externalId != null) {
      assumeRequest = assumeRequest.withExternalId(externalId);
    }
    AssumeRoleResult assumeResult =
            stsClient.assumeRole(assumeRequest);
    this.accessKey = assumeResult.getCredentials().getAccessKeyId();
    this.secretKey = assumeResult.getCredentials().getSecretAccessKey();
    this.sessionToken = assumeResult.getCredentials().getSessionToken();
    this.expirationDate = assumeResult.getCredentials().getExpiration();
  }

  private boolean isExpired(){
    Calendar instance = Calendar.getInstance();
    // TODO : this is being done to avoid race conditions, need to explore a better way
    instance.add(Calendar.MINUTE, 5);
    Date now = instance.getTime();
    if (expirationDate.compareTo(now) < 0) {
      return true;
    }
    return false;
  }

  @Override
  protected String getTypeName() {
    return "session";
  }

  @Override
  protected String getVersionPrefix() { return "jets3t AWS Credentials, version: "; }

  /**
   * @return
   * The AWS session token for temporary/session-based account credentials.
   */
  @Override
  public String getSessionToken() {
    return this.sessionToken;
  }

  @Override
  public synchronized ProviderCredentials refreshAndGetCredentials() {
    if(isExpired() == true) {
      assumeRoleAndGetCredentials();
    }
    return new AWSCredentials(accessKey, secretKey){

      @Override
      public String getSessionToken() {
        return sessionToken;
      }

    };
  }

}
