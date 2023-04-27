#include <Ice/Ice.h>
#include <Servants.h>

using namespace std;
using namespace Demo;

class DemoI : public DemoInterface{
public:
    virtual void sayHello(const Ice::Current& current) override {
        std::cout << "Hello from DemoObject!" << std::endl;
    }
};

int main(int argc, char* argv[]) {
    try {
        Ice::CommunicatorHolder ich(argc, argv);

        Ice::ObjectAdapterPtr adapter = ich->createObjectAdapterWithEndpoints("DemoAdapter", "default -p 10000");

        Ice::ObjectPtr object = new DemoI;
        adapter->add(object, ich->stringToIdentity("DemoAdapter"));
        // auto demoObjectProxy = adapter->addWithUUID(demoObject)->ice_twoway()->ice_secure(false)->ice_connectionCached(false);

        adapter->activate();

        std::cout << "Server started" << std::endl;
        ich->waitForShutdown();
    } catch (const std::exception& e) {
        std::cerr << e.what() << std::endl;
        return 1;
    }
    return 0;
}
