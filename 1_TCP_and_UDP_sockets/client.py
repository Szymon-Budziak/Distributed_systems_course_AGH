import socket
import threading

SERVER_HOST = '0.0.0.0'
SERVER_PORT = 8050
FORMAT = 'utf-8'


def receive_message(sock: socket) -> None:
    while True:
        try:
            message = sock.recv(1024).decode(FORMAT)
            print(message)
        except OSError:
            break


def main() -> None:
    client_name = input('Enter your name: ')
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.connect((SERVER_HOST, SERVER_PORT))
    sock.send(client_name.encode(FORMAT))
    receive_thread = threading.Thread(target=receive_message, args=(sock,))
    receive_thread.start()
    while True:
        try:
            message = input()
            if message == 'quit':
                break
            if message.startswith('M '):
                message = f'MULTICAST MESSAGE from {client_name}: {message[2:]}'
                sock.send(message.encode(FORMAT))
            elif message.startswith('U '):
                udp_message = f"UDP MESSAGE from {client_name}: {message[2:]}"
                udp_sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
                udp_sock.sendto(udp_message.encode(FORMAT), (SERVER_HOST, SERVER_PORT))
                udp_sock.close()
            else:
                message = f'{client_name}: {message}'
                sock.send(message.encode(FORMAT))
        except KeyboardInterrupt:
            break
    sock.close()


if __name__ == '__main__':
    main()
