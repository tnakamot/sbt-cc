#include <stdio.h>

#include "tmp_header.h"
#include "dynamic.h"

int main(int argc, char *argv[])
{
  printf("Hello world.\n");
  printf("Macro value from an external header: " EXTERNAL_VALUE ".\n");

  dynamic_func();
}
