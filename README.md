# Darknet Data Creator

This project is made for people who want to create their own data for the `Darknet` framework.
What this `program` does is that it will use our `USB camera` or your laptop `camera` to take pictures
in a desired sampling interval. You don't need to create any labels and the bounded boxes. This `program` will
do it for you.

`Darknet Data Creator` can also train a model for you by specify where your `.cfg`, `.weights`, `.data` and `darknet` or `darknet.exe` files are.

# Dataset

I have collected my own dataset where i record a lots of things by using this software. All images are `416x416` with sample time `2 seconds`.

https://github.com/DanielMartensson/Pictures-Of-Things

# How to use

Begin first to install Maven. Or else, you cannot run this `program`.

```
sudo apt-get install maven -y
```

Then you need to download `Darknet Data Creator` file and `unzip` that `.zip` file. After that, stand inside the project folder and write

```
mvn javafx:run
```

Done!

# How it's look like

Collecting data. This red bounded box will not be included inside the training data.

![a](https://raw.githubusercontent.com/DanielMartensson/Darknet-Data-Creator/main/pictures/collecting.png)

Specify your `.cfg`, `.weights`, `.data` and `darknet` or `darknet.exe` files.

![a](https://raw.githubusercontent.com/DanielMartensson/Darknet-Data-Creator/main/pictures/training.png)

Here is the result

![a](https://raw.githubusercontent.com/DanielMartensson/Darknet-Data-Creator/main/pictures/result.png)

Also the paths for every `.png` pictures are listed.

![a](https://raw.githubusercontent.com/DanielMartensson/Darknet-Data-Creator/main/pictures/paths.png)

# Test with two classes from dataset Pictures-Of-Things with Yolov4 Tiny

This is a simple quick test where I had 344 training pictures of `cup` and `box` and 87 for validation. Total 431 batches. 
It was quite difficult to train for the `box` due to the high variation of these boxes, but the `cup` did a great job due to the similarities.

It took me about 30 minutes to create two classes and create a model.

![a](https://raw.githubusercontent.com/DanielMartensson/Darknet-Data-Creator/main/pictures/cup.jpg)

![a](https://raw.githubusercontent.com/DanielMartensson/Darknet-Data-Creator/main/pictures/box.jpg)

