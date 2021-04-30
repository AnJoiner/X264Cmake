//
// Created by c2yu on 2021/4/30.
//

#ifndef X264CMAKE_SAFE_QUEUE_H
#define X264CMAKE_SAFE_QUEUE_H


#endif //X264CMAKE_SAFE_QUEUE_H

enum queue_empty_status{
    QUEUE_NOT_EMPTY = 0,
    QUEUE_EMPTY = 1
};

typedef struct QNode{
    char *data;
    struct QNode *next;
} QNode;

//定义队列结构
typedef struct {
    // 指向开头
    QNode *front;
    // 指向尾部
    QNode *rear;
    // 队列长度
    int length;
} LinkedQueue;

/**
 * create the linked queue.
 * @return linked queue
 */
LinkedQueue *create_queue();
/**
 * push data into the linked queue
 * @param queue
 * @param data
 * @return 0 : success 1:failure
 */
int push_data(LinkedQueue *queue, char *data);

/**
 *
 * @param queue
 * @return the queue is empty? QUEUE_EMPTY or QUEUE_NOT_EMPTY
 */
int queue_is_empty(LinkedQueue *queue);

/**
 * pop data from the linked queue
 * @param queue
 * @return data
 */
char *pop_data(LinkedQueue *queue);



