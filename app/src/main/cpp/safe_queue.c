//
// Created by c2yu on 2021/4/29.
//
#include <stdio.h>
#include <string.h>
#include <malloc.h>
#include "safe_queue.h"
#include "android/log.h"


#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , "h264-encode", __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO  , "h264-encode", __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN  , "h264-encode", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR  , "h264-encode", __VA_ARGS__)


LinkedQueue *create_queue(){
    LinkedQueue* queue = (LinkedQueue*)malloc(sizeof(LinkedQueue));
    if (!queue) {
        LOGE("No enough memory to allocate queue space");
        return NULL;
    }
    queue->front = NULL;
    queue->rear = NULL;
    queue->length = 0;
    return queue;
}


int push_data(LinkedQueue *queue, char *data, int data_size) {
    LOGI("start push data into queue...");
    QNode *node = (QNode *) malloc(sizeof(QNode));
    if (node == NULL)    /* 判断分配内存是否失败 */
    {
        LOGE("No enough memory to allocate node space");
        return -1;
    }
    node->data = data;
    node->size = data_size;
    node->next = NULL;
    if (queue->front == NULL){
        queue->front = node;
    }
    if (queue->rear == NULL){
        queue->rear = node;
    } else{
        queue->rear->next = node;
        queue->rear = node;
    }
    queue->length++;
    LOGI("push data into queue success");
    return queue->length;
}

int queue_is_empty(LinkedQueue *queue) {
    return (queue->front == NULL);
}

QNode *pop_data(LinkedQueue *queue) {
    LOGI("start pop data from queue...");
    char *data = NULL;
    // 队列为空无法出栈
    if (queue_is_empty(queue)) {
        LOGE("the queue is empty, can not pop!");
        return data;
    }

    QNode *temp = queue->front;
    if (queue->front == queue->rear){
        // 只有一个元素
        queue->front = NULL;
        queue->rear = NULL;
        queue->length = 0;
    } else{
        queue->front = queue->front->next;
        queue->length--;
    }
//    data = temp->data;
//    free(temp);
    LOGI("pop data from queue success");
    return temp;
}


int free_queue(LinkedQueue *queue){
    int val = 0;

    while (queue->length > 0){
      QNode * node = pop_data(queue);
      if (node!=NULL) {
          if (node->data!=NULL) free(node->data);
          free(node);
      }
    }
    free(queue);
    LOGI("Succeed to release queue!");
    return val;
}


