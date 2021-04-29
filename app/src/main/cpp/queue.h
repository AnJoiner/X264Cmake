//
// Created by c2yu on 2021/4/29.
//

#ifndef X264CMAKE_QUEUE_H
#define X264CMAKE_QUEUE_H

#endif //X264CMAKE_QUEUE_H

#include "jni.h"

typedef struct Queue{
    jbyte *buff;
    Queue *next;
};