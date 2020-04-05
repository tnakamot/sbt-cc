#include <stdio.h>

#include "util.h"
#include "util_aux.h"

int main(int argc, char *argv[])
{
  printf("This is " PROGRAM_NAME ".\n");
  util();
  util_aux();
}