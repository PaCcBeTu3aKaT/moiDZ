razmer = int (input ("Введите размер клетки шахматной доски: "))

kolichestvo_kletok = 8

for stroka in range (kolichestvo_kletok):
    for podstroka in range (razmer):
        liniya = ""
        for stolbec in range (kolichestvo_kletok):
            if (stroka + stolbec) % 2 == 0:
                liniya += "*" * razmer
            else:
                liniya += "-" * razmer
        print (liniya)
