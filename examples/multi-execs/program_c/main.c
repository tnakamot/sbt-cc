#include <stdio.h>
#include <math.h>

#include "util.h"
#include "util_aux.h"

int main(int argc, char *argv[])
{
  printf("This is " PROGRAM_NAME ".\n");
  printf("sqrt(5) is %lf\n", sqrt(5.0));
  util();
  util_aux();
}