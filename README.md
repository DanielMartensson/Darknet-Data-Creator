# Darknet Data Creator

This project is made for people who want to create their own data for the `Darknet` framework.
What this `program` does is that it will use our `USB camera` or your laptop `camera` to take pictures
in a desired sampling interval. You don't need to create any labels and the bounded boxes. This `program` will
do it for you.

`Darknet Data Creator` can also train a model for you by specify where your `.cfg`, `.weights`, `.data` and `darknet` or `darknet.exe` files are.

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
