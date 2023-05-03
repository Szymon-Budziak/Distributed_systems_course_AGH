#include <Ice/Ice.h>
#include <calculator.h>

using namespace std;
using namespace Math;

class CalculatorI : public Calculator {
public:
    long add(int a, int b, const Ice::Current &current) override {
        cout << "ADD: a = " << a << ", b = " << b << ", result = " << (a + b) << endl;

        return a + b;
    }

    long subtract(int a, int b, const Ice::Current &current) override {
        cout << "SUBTRACT: a = " << a << ", b = " << b << ", result = " << (a - b) << endl;

        return a - b;
    }

    float multiply(const ThreeNumbers &a, float b, const Ice::Current &current) override {
        cout << "MULTIPLY: a = " << a.n1 << ", b = " << a.n2 << ", c = " << a.n3 << ", d = " << b << ", result = "
             << (a.n1 * a.n2 * a.n3 * b) << endl;

        return a.n1 * a.n2 * a.n3 * b;
    }
};

int main(int argc, char *argv[]) {
    int status = 0;
    Ice::CommunicatorPtr communicator;

    try {
        communicator = Ice::initialize(argc, argv);

        Ice::ObjectAdapterPtr adapter = communicator->createObjectAdapterWithEndpoints("Adapter",
                                                                                       "tcp -h 127.0.0.2 -p 10000 -z : udp -h 127.0.0.2 -p 10000 -z");
        Ice::ObjectPtr calculatorServant = new CalculatorI;

        adapter->add(calculatorServant, communicator->stringToIdentity("new_calc"));

        adapter->activate();

        cout << "Entering event processing loop..." << endl;

        communicator->waitForShutdown();
    } catch (const Ice::Exception &e) {
        cerr << e << endl;
        status = 1;
    } catch (const char *msg) {
        cerr << msg << endl;
        status = 1;
    }

    if (communicator) {
        try {
            communicator->destroy();
        } catch (const Ice::Exception &e) {
            cerr << e << endl;
            status = 1;
        }
    }

    return status;
}