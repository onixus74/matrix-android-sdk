/*
 * Copyright 2016 OpenMarket Ltd
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

package org.matrix.androidsdk.crypto;

import android.text.TextUtils;

import org.matrix.androidsdk.util.Log;

import org.matrix.androidsdk.crypto.algorithms.IMXDecrypting;
import org.matrix.androidsdk.crypto.algorithms.IMXEncrypting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MXCryptoAlgorithms {

    private static final String LOG_TAG = MXCryptoAlgorithms.class.getSimpleName();

    /**
     * Matrix algorithm tag for olm.
     */
    public static final String MXCRYPTO_ALGORITHM_OLM = "m.olm.v1.curve25519-aes-sha2";

    /**
     * Matrix algorithm tag for megolm.
     */
    public static final String MXCRYPTO_ALGORITHM_MEGOLM = "m.megolm.v1.aes-sha2";

    // encryptors map
    private final HashMap<String, Class<IMXEncrypting>> mEncryptors;

    // decryptors map
    private final HashMap<String, Class<IMXDecrypting>> mDecryptors;

    // shared instance
    private static MXCryptoAlgorithms mSharedInstance = null;

    /**
     * @return the shared instance
     */
    public static MXCryptoAlgorithms sharedAlgorithms() {
        if (null == mSharedInstance) {
            mSharedInstance = new MXCryptoAlgorithms();
        }

        return mSharedInstance;
    }

    /**
     * Constructor
     */
    private MXCryptoAlgorithms() {
        // encryptos
        mEncryptors = new HashMap<>();
        try {
            mEncryptors.put(MXCRYPTO_ALGORITHM_MEGOLM, (Class<IMXEncrypting>) Class.forName("org.matrix.androidsdk.crypto.algorithms.megolm.MXMegolmEncryption"));
        } catch (Exception e) {
            Log.e(LOG_TAG, "## MXCryptoAlgorithms() : fails to add MXCRYPTO_ALGORITHM_MEGOLM " + e.getMessage());
        }

        try {
            mEncryptors.put(MXCRYPTO_ALGORITHM_OLM, (Class<IMXEncrypting>) Class.forName("org.matrix.androidsdk.crypto.algorithms.olm.MXOlmEncryption"));
        } catch (Exception e) {
            Log.e(LOG_TAG, "## MXCryptoAlgorithms() : fails to add MXCRYPTO_ALGORITHM_OLM " + e.getMessage());
        }

        mDecryptors = new HashMap<>();
        try {
            mDecryptors.put(MXCRYPTO_ALGORITHM_MEGOLM, (Class<IMXDecrypting>) Class.forName("org.matrix.androidsdk.crypto.algorithms.megolm.MXMegolmDecryption"));
        } catch (Exception e) {
            Log.e(LOG_TAG, "## MXCryptoAlgorithms() : fails to add MXCRYPTO_ALGORITHM_MEGOLM " + e.getMessage());
        }

        try {
            mDecryptors.put(MXCRYPTO_ALGORITHM_OLM, (Class<IMXDecrypting>) Class.forName("org.matrix.androidsdk.crypto.algorithms.olm.MXOlmDecryption"));
        } catch (Exception e) {
            Log.e(LOG_TAG, "## MXCryptoAlgorithms() : fails to add MXCRYPTO_ALGORITHM_OLM " + e.getMessage());
        }
    }

    /**
     * Get the class implementing encryption for the provided algorithm.
     *
     * @param algorithm the algorithm tag.
     * @return A class implementing 'IMXEncrypting'.
     */
    public Class<IMXEncrypting> encryptorClassForAlgorithm(String algorithm) {
        if (!TextUtils.isEmpty(algorithm)) {
            return mEncryptors.get(algorithm);
        } else {
            return null;
        }
    }

    /**
     * Get the class implementing decryption for the provided algorithm.
     *
     * @param algorithm the algorithm tag.
     * @return A class implementing 'IMXDecrypting'.
     */

    public Class<IMXDecrypting> decryptorClassForAlgorithm(String algorithm) {
        if (!TextUtils.isEmpty(algorithm)) {
            return mDecryptors.get(algorithm);
        } else {
            return null;
        }
    }

    /**
     * @return The list of registered algorithms.
     */
    public List<String> supportedAlgorithms() {
        return new ArrayList<>(mEncryptors.keySet());
    }
}