import requests
import random
import sys
from http import HTTPStatus
from threading import Thread

from user import *
from booking import *
from wallet import *

debited_amount = 0
credited_amount = 0


# Thread 1: Credits into user's wallet
def t1(user_id):
    global credited_amount
    for i in range(100):
        amount = random.randint(10, 100)
        response = requests.put(walletServiceURL + f"/wallets/{user_id}", json={'action': 'credit', 'amount': amount})
        if response.status_code == 200:
            credited_amount += amount


# Thread 2: Debits from user's wallet
def t2(user_id):
    global debited_amount
    for i in range(100):
        amount = random.randint(5, 50)
        response = requests.put(walletServiceURL + f"/wallets/{user_id}", json={'action': 'debit', 'amount': amount})
        if response.status_code == 200:
            debited_amount += amount


def main():
    try:
        response = delete_users()
        if not check_response_status_code(response, 200):
            return False

        response = delete_wallets()
        if not check_response_status_code(response, 200):
            return False

        response = post_user('Anurag Kumar', 'ak47@iisc.ac.in')
        if not test_post_user('Anurag Kumar', 'ak47@iisc.ac.in', response):
            return False
        user = response.json()

        initial_balance = 100
        response = put_wallet(user['id'], 'credit', initial_balance)
        if not test_put_wallet(user['id'], 'credit', initial_balance, 0, response):
            return False

        ### Parallel Execution Begins ###
        thread1 = Thread(target=t1, kwargs={'user_id': user['id']})
        thread2 = Thread(target=t2, kwargs={'user_id': user['id']})

        thread1.start()
        thread2.start()

        thread1.join()
        thread2.join()
        ### Parallel Execution Ends ###

        response = get_wallet(user['id'])
        if not test_get_wallet(user['id'], response, True, initial_balance + credited_amount - debited_amount):
            return False

        return True
    except:
        return False


if __name__ == "__main__":
    if main():
        sys.exit(0)
    else:
        sys.exit(1)
