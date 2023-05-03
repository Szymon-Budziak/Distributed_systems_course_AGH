module Math
{
  struct ThreeNumbers
  {
    short n1;
    int n2;
    long n3;
  };

  interface Calculator
  {
    long add(int a, int b);
    long subtract(int a, int b);
    float multiply(ThreeNumbers a, float b);
  };
};