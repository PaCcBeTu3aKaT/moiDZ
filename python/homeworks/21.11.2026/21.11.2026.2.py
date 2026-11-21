tekst = input ("Введите текст: ")
zarezervirovannyeInput = input ("Введите зарезервированные слова через пробел: ")
zarezervirovannyeSlova = zarezervirovannyeInput.lower ().split ()

slovaTeksta = tekst.split ()
rezultat = []

for slovo in slovaTeksta:
    chistoeSlovo = slovo.strip (".,!?;:-()\"'")
    if chistoeSlovo.lower () in zarezervirovannyeSlova:
        rezultat.append (slovo.replace (chistoeSlovo, chistoeSlovo.upper ()))
    else:
        rezultat.append (slovo)

print (" ".join (rezultat))
