import requests
import random
import sys
from http import HTTPStatus
from threading import Thread

from user import *
from booking import *
from wallet import *

user1_seats_booked = 0
user2_seats_booked = 0

def reset_seats_booked(thread_id):
    global user1_seats_booked
    global user2_seats_booked
    
    if thread_id == 1:
        user1_seats_booked = 0
    elif thread_id == 2:
        user2_seats_booked = 0

def increment_seats_booked(thread_id, value):
    global user1_seats_booked
    global user2_seats_booked
    
    if thread_id == 1:
        user1_seats_booked += value
    elif thread_id == 2:
        user2_seats_booked += value

def get_seats_booked(thread_id):
    global user1_seats_booked
    global user2_seats_booked
    
    if thread_id == 1:
        return user1_seats_booked
    elif thread_id == 2:
        return user2_seats_booked
        
def t(thread_id, user_id, show_id):
    for i in range(10):
        response = requests.get(bookingServiceURL + f"/shows/{show_id}")
        assert response.status_code == 200
        show = response.json()
        
        # Current value of seats avaiable for show
        seats_available = show['seats_available']
        # Number of seats booked by thread 'thread_id'
        seats_booked = get_seats_booked(thread_id)

        # If no seats are available and if current thread succesfully 
        # created bookings in previous iterations, then, delete all previous bookings
        if seats_available == 0 and seats_booked > 0:
            response = requests.delete(f"{bookingServiceURL}/bookings/users/{user_id}/shows/{show_id}")
            assert response.status_code == 200
            reset_seats_booked(thread_id)        
        elif seats_available > 0:
            # Flag 'should_delete' determines whether to make a new booking
            # or delete all previous bookings in this loop iteration
            should_delete = random.randint(0,1)
            if seats_booked == 0:
                should_delete = 0
                
            if should_delete == 1:
                response = requests.delete(f"{bookingServiceURL}/bookings/users/{user_id}/shows/{show_id}")
                assert response.status_code == 200
                reset_seats_booked(thread_id)
            else:
                no_seats = random.randint(1, min(60, seats_available))
                response = requests.post(f"{bookingServiceURL}/bookings", json={'show_id': show_id, 'user_id': user_id, 'seats_booked': no_seats})
                if response.status_code == 200:
                    increment_seats_booked(thread_id, no_seats)

def main():
    try:
        response = delete_users()
        if not check_response_status_code(response, 200):
            return False
    
        response = delete_wallets()
        if not check_response_status_code(response, 200):
            return False

        response = post_user('Anurag Kumar','ak47@iisc.ac.in')
        if not test_post_user('Anurag Kumar','ak47@iisc.ac.in', response):
            return False
        user1 = response.json()

        response = post_user('Rajanya Dasgupta','rdasgputa@iisc.ac.in')
        if not test_post_user('Rajanya Dasgupta','rdasgputa@iisc.ac.in', response):
            return False
        user2 = response.json()

        initial_balance = 10000
        response = put_wallet(user1['id'], 'credit', initial_balance)
        if not test_put_wallet(user1['id'], 'credit', initial_balance, 0, response):
            return False

        response = put_wallet(user2['id'], 'credit', initial_balance)
        if not test_put_wallet(user2['id'], 'credit', initial_balance, 0, response):
            return False

        show_id = 4
        response = get_show(show_id)
        if not test_get_show(response):
            return False
        show = response.json()
        initial_seats = show['seats_available']
        
        ### Parallel Execution Begins ###
        thread1 = Thread(target=t, kwargs = {'thread_id': 1, 'user_id': user1['id'], 'show_id': show_id})
        thread2 = Thread(target=t, kwargs = {'thread_id': 2, 'user_id': user2['id'], 'show_id': show_id})

        thread1.start()
        thread2.start()

        thread1.join()
        thread2.join()
        ### Parallel Execution Ends ###

        response = get_show(show_id)
        if not test_get_show(response):
            return False
        show = response.json()
        if not check_field_value(show, 'seats_available', initial_seats - user1_seats_booked - user2_seats_booked):
            return False

        return True
    except:
        return False

if __name__ == "__main__":
    if main():
        sys.exit(0)
    else:
        sys.exit(1)
