#include <stdio.h>

int main(int argc, char *argv[]) {
  printf("Hello world!!!\n");

  if (argc >= 2) {
    printf("First argument: %s\n", argv[1]);
    if (argc >= 3) {
      printf("Second argument: %s\n", argv[2]);
    }
  } else {
    printf("No argument was specified\n");
  }
}
