### Running the whole project

To run the whole project, you have to follow these steps:

1. Slice **.ice** file to js and cpp:

```bash
slice2js --output-dir client/ slice/Servants.ice
slice2cpp --output-dir server/ slice/Servants.ice
```

2. Create **.cpp**, **.h** and **.o** files:

```bash
c++ -I. -c server/Servants.cpp server/Server.cpp
c++ -o server/server server/Servants.o server/Server.o -lIce
```

3. Run server:

```bash
./server/server
```

4. Run client:

```bash
node client/client.js
```

#### Credits

This project was created by Szymon Budziak.