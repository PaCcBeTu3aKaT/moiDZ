n = int (input ("Введите размер ромба (число строк верхней половины): "))

for i in range (1, n + 1):
    probel = " " * (n - i)
    zvezdy = "*" * (2 * i - 1)
    print (probel + zvezdy)

for i in range (n - 1, 0, -1):
    probel = " " * (n - i)
    zvezdy = "*" * (2 * i - 1)
    print (probel + zvezdy)
