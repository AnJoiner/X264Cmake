//
// Created by c2yu on 2021/5/8.
//

#ifndef X264CMAKE_FDK_AAC_CORE_H
#define X264CMAKE_FDK_AAC_CORE_H

#endif //X264CMAKE_FDK_AAC_CORE_H
enum {
    FDKAAC_ENC_OK = 0,
    FDKAAC_ENC_FAIL = -1
};

enum {
    FDKAAC_ENC_UNINITIALIZED,
    FDKAAC_ENC_INITIALIZED
};

enum {
    FDKAAC_ENC_ENCODING,
    FDKAAC_ENC_STOPPED
};


/**
 * open the aac encode and config the params
 * @param sample_rate
 * @param channel
 * @param bitrate
 * @param aac_path
 * @return 0 success -1 failure
 */
int fdk_aac_enc_init(int sample_rate, int channel, int bitrate,const char *aac_path);

/**
 * encode per audio frame
 * @param in_data
 * @param size
 * @return 0 success -1 failure
 */
int fdk_aac_enc_data(char *buffer, int size);

/**
 * release the aac encode.
 * @return 0 success -1 failure
 */
void fdk_aac_enc_release();