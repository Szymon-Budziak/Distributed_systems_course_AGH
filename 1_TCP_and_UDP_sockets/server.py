import socket
import threading
import select

SERVER_HOST = '0.0.0.0'  # Listen on all network interfaces
SERVER_PORT = 8050
FORMAT = 'utf-8'

clients = {}
clients_lock = threading.Lock()


def broadcast_udp_message(ascii_art: str, sender: str) -> None:
    with clients_lock:
        for client, client_sock in clients.items():
            if client != sender:
                client_sock.send(ascii_art.encode(FORMAT))


def broadcast_tcp_message(message: str, sender: str) -> None:
    with clients_lock:
        for client, client_sock in clients.items():
            if client != sender:
                client_sock.send(message.encode(FORMAT))


def handle_udp_connection(data: bytes) -> None:
    sender = data.decode(FORMAT).replace(":", "").split()[3]
    print("UDP message received")
    with open('asci_art.txt', 'r') as f:
        ascii_art = f.read()
    broadcast_udp_message(ascii_art, sender)


def handle_tcp_connection(client_sock: socket, client_addr: tuple) -> None:
    client_name = client_sock.recv(1024).decode(FORMAT)
    with clients_lock:
        clients[client_name] = client_sock
    print(f'{client_name} connected from {client_addr}')
    broadcast_tcp_message(f'{client_name} joined the chat', client_name)
    while True:
        try:
            message = client_sock.recv(1024).decode(FORMAT)
            if message.startswith('MULTICAST MESSAGE'):
                print("MULTICAST message received")
                message = message[18:]
                broadcast_tcp_message(message, '')
            else:
                broadcast_tcp_message(message, client_name)
        except OSError:
            with clients_lock:
                del clients[client_name]
            print(f'{client_name} disconnected')
            broadcast_tcp_message(f'{client_name} left the chat', client_name)
            break


def main() -> None:
    tcp_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    tcp_socket.bind((SERVER_HOST, SERVER_PORT))
    tcp_socket.listen()

    udp_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    udp_socket.bind((SERVER_HOST, SERVER_PORT))

    sockets_list = [tcp_socket, udp_socket]

    print(f'Server listening on {SERVER_HOST}:{SERVER_PORT}...')
    while True:
        try:
            read_sockets, _, _ = select.select(sockets_list, [], [])
            for sock in read_sockets:
                if sock == tcp_socket:
                    client_sock, client_addr = tcp_socket.accept()
                    tcp_client_thread = threading.Thread(target=handle_tcp_connection, args=(client_sock, client_addr))
                    tcp_client_thread.start()
                elif sock == udp_socket:
                    data, _ = udp_socket.recvfrom(1024)
                    udp_client_thread = threading.Thread(target=handle_udp_connection, args=(data,))
                    udp_client_thread.start()

        except KeyboardInterrupt:
            break

    tcp_socket.close()
    udp_socket.close()


if __name__ == '__main__':
    main()
