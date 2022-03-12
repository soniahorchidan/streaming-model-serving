import tensorflow as tf
from tensorflow import keras
# Helper libraries
import numpy as np
import matplotlib.pyplot as plt
import os
import subprocess
import random
import json
import requests


def run_tensorflow_serving():
    fashion_mnist = keras.datasets.fashion_mnist
    (train_images, train_labels), (test_images, test_labels) = fashion_mnist.load_data()

    # scale the values to 0.0 to 1.0
    train_images = train_images / 255.0
    test_images = test_images / 255.0

    # reshape for feeding into the model
    train_images = train_images.reshape(train_images.shape[0], 784)
    test_images = test_images.reshape(test_images.shape[0], 784)

    class_names = ['T-shirt/top', 'Trouser', 'Pullover', 'Dress', 'Coat',
                   'Sandal', 'Shirt', 'Sneaker', 'Bag', 'Ankle boot']

    data = json.dumps({"signature_name": "serving_default", "instances": test_images[0:3].tolist()})
    serve_request(data, class_names, test_labels, test_images)


def serve_request(data, class_names, test_labels, test_images):
    headers = {"content-type": "application/json"}
    json_response = requests.post('http://localhost:8501/v1/models/fashion_model:predict', data=data, headers=headers)
    predictions = json.loads(json_response.text)['predictions']

    for i in range(0, 3):
        show(i, 'The model thought this was a {} (class {}),\n and it was actually a {} (class {})'.format(
            class_names[np.argmax(predictions[i])], np.argmax(predictions[i]), class_names[test_labels[i]],
            test_labels[i]), test_images=test_images)


def show(idx, title, test_images):
    plt.figure()
    plt.imshow(test_images[idx].reshape(28, 28))
    plt.axis('off')
    plt.title('\n\n{}'.format(title), fontdict={'size': 10})
    plt.show()


# Press the green button in the gutter to run the script.
if __name__ == '__main__':
    run_tensorflow_serving()
