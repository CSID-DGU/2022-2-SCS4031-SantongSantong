import datetime
import threading
import time
from channels.generic.websocket import WebsocketConsumer
from channels.db import database_sync_to_async
from channels.layers import get_channel_layer
import json
from asgiref.sync import async_to_sync

from django.db.models.signals import post_save
from django.dispatch import receiver

import os
import sys
sys.path.append("/home/irteam/2022-2-SCS4031-SantongSantong/backend")
import django

os.environ.setdefault("DJANGO_SETTINGS_MODULE", "backend.settings")
django.setup()

from notification.models import Notification
from notification.serializers import NotificationSerializer
# from notification.my_webcam_demo_stdet_ensemble import main

def get_notification():
    notifications = Notification.objects.all()
    serializer = NotificationSerializer(notifications, many=True)
    return serializer

def make_notification():
    notification = Notification(
        area_id=1,
        pub_date=datetime.datetime.now(),
        image="./static/drowning.jpg",
    )
    notification.save()


@receiver(post_save, sender=Notification)
def send_update(sender, instance, created, **kwargs):
    print("New reading in DB")
    serializer = NotificationSerializer(instance)

    if created:
        print("New saving in DB")
        print(serializer.data)
        channel_layer = get_channel_layer()
        async_to_sync(channel_layer.group_send)(
            "notification_1", {"type": "notify", "data": serializer.data}
        )


class NotificationConsumer(WebsocketConsumer):
    def connect(self):
        self.area_name = self.scope["url_route"]["kwargs"]["area_name"]  # notification/routing.py에 있는 center_name
        self.group_name = 'notification_%s' % self.area_name
        print(self.area_name)
        print(self.group_name)
        async_to_sync(self.channel_layer.group_add)(  # group 참여
            self.group_name, self.channel_name
        )

        self.accept()  # websocket 연결
        threading.Timer(20, make_notification).start()

    def disconnect(self, close_code):
        # Leave group
        async_to_sync(self.channel_layer.group_discard)(
            self.group_name, self.channel_name
        )

    
    def notify(self, event):
        data = event['data']
        self.send(text_data=json.dumps(data))

    