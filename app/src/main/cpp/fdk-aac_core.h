//
// Created by c2yu on 2021/5/8.
//

#ifndef X264CMAKE_FDK_AAC_CORE_H
#define X264CMAKE_FDK_AAC_CORE_H

#endif //X264CMAKE_FDK_AAC_CORE_H

/**
 * open the aac encode and config the params
 * @param sample_rate
 * @param channel
 * @param bitrate
 * @param aac_path
 * @return 0 success -1 failure
 */
int fdk_aac_open(int sample_rate, int channel, int bitrate, char *aac_path);

/**
 * encode per audio frame
 * @param in_data
 * @param size
 * @return 0 success -1 failure
 */
int fdk_aac_encode(char *in_data, int size);
/**
 * encode per audio frame
 * @param in_data
 * @param size
 * @return 0 success -1 failure
 */
int fdk_aac_encode_data(char *in_data, int size);

/**
 * close the aac encode.
 * @return 0 success -1 failure
 */
int fdk_aac_close();