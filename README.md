# BeginFun
Simple programing language created with ANTLR4 Visitor.

#### Tools
    Oracle OpenJDK 17.0.2
    Apache Maven 3.8.5
    IntelliJ IDEA Ultimate 2021.3.3
        - ANTLR v4 plugin

#### Build

Generate ANTLR visitor with:

> mvn clean antlr4:antlr4@generate-visitor

Build jar with:

> mvn clean package

Run jar with:

> java -jar target/beginFun-1.0.jar samples/test.beginFun

#### Sample code
    fun FibRec(Int n) => Int begin fun
        if(n <= 1) then begin if
            break fun => n
        end if
    end fun => FibRec(n - 1) + FibRec(n - 2)

    fun FibLoop(Int n) => Int begin fun
        Int count = 2;
        Int sum = 0;
        Int a = 0;
        Int b = 1;
        while(count <= n) do
            sum = a + b;
            a = b;
            b = sum;
            count = count + 1;
        end while
    end fun => sum

    print("Calculating Fibonacci sequence with recursion...");
    write("Test.txt", "Fibonacci with recursion: ", false);
    Int c = FibRec(10);
    write("Test.txt", c, true);
    print("Writing...");

    print("Calculating Fibonacci sequence with while loop...");
    write("Test.txt", "\nFibonacci with loop: ", true);
    Int d = FibLoop(10);
    print("Writing...");
    write("Test.txt", d, true);