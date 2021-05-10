//
// Created by c2yu on 2021/5/10.
//

#ifndef X264CMAKE_FAAC_ENCODE_H
#define X264CMAKE_FAAC_ENCODE_H

#endif //X264CMAKE_FAAC_ENCODE_H

enum {
    FAAC_ENC_OK = 0,
    FAAC_ENC_FAIL = -1
};

enum {
    FAAC_ENC_UNINITIALIZED,
    FAAC_ENC_INITIALIZED
};

enum {
    FAAC_ENC_ENCODING,
    FAAC_ENC_STOPPED
};

enum {
    FAAC_OUT_RAW = 0,
    FAAC_OUT_ADTS = 1
};

/**
 * open the aac encode and config the params
 * @param sample_rate
 * @param channel
 * @param bitrate
 * @param aac_path
 * @return 0 success -1 failure
 */
int faac_enc_init(unsigned long sample_rate, unsigned long  channel, unsigned long  bit_rate, unsigned long  pcm_bit_size,
                  const char *aac_path);

/**
 * encode per audio frame
 * @param in_data
 * @param size
 * @return 0 success -1 failure
 */
int faac_enc_data(char *buffer, int size);

/**
 * release the aac encode.
 * @return 0 success -1 failure
 */
void faac_enc_release();