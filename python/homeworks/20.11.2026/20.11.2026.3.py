dniNedeli = ["Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье"]

try:
    nomerDnya = int (input ("Введите номер дня недели: "))
    if nomerDnya < 1 or nomerDnya > 7:
        raise IndexError
    print (dniNedeli[nomerDnya - 1])
except ValueError:
    print ("Введите номер дня")
except IndexError:
    print ("Нет такого дня")
finally:
    print ("Проверка завершена")
