import ast

import numpy as np
import torch

import model

torch.set_num_threads(1)


def accuracy(outputs, labels):
    pass


class MNISTFashionClassifier(object):
    """
    MNISTDigitClassifier handler class. This handler takes a greyscale image
    and returns the digit in that image.
    """

    def __init__(self):
        super().__init__()
        self.model = None
        self.device = 'cpu'

    def initialize(self, ctx):
        input_size = 784
        num_classes = 10
        num_hidden_layers = 3
        hidden_size = 32

        self.model = model.FFNN(input_size, num_hidden_layers, hidden_size, out_size=num_classes,
                                accuracy_function=accuracy)
        state_dict = torch.load("ffnn.torch")
        self.model.load_state_dict(state_dict)
        self.model.to(self.device)
        self.model.eval()
        self.model.share_memory()

    def handle(self, data, context):
        with torch.no_grad():
            images = ast.literal_eval(data[0]['body'].decode("utf-8"))
            images = np.array(images).astype(np.float32)
            images = torch.from_numpy(images)
            images = images.to(self.device)
            self.model.forward(images)
            fake_output = ["0"] * len(images)
            return [",".join(fake_output)]
