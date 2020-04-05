#include <stdio.h>
#include <math.h>
#include <pthread.h>

#include "util.h"
#include "util_aux.h"

int main(int argc, char *argv[])
{
  pthread_t thread_1, thread_2;

  printf("This is " PROGRAM_NAME " " VERSION ".\n");
  printf("sqrt(3) is %lf\n", sqrt(3.0));

  pthread_create(&thread_1, NULL, (void *)util, NULL);
  pthread_create(&thread_2, NULL, (void *)util_aux, NULL);

  pthread_join(thread_1, NULL);
  pthread_join(thread_2, NULL);
}