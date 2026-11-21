tekst = input ("Введите текст: ")
koncyPredlozheniy = ".!?"
kolichestvoPredlozheniy = 0
vKonsePredlozheniya = False

for simvol in tekst:
    if simvol in koncyPredlozheniy:
        if not vKonsePredlozheniya:
            kolichestvoPredlozheniy += 1
            vKonsePredlozheniya = True
    else:
        vKonsePredlozheniya = False

print ("Количество предложений:", kolichestvoPredlozheniy)
