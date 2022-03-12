from abc import ABC
import ast
import numpy as np
import torch
from ts.torch_handler.base_handler import BaseHandler

import model

MODEL_WEIGHTS = "/home/sfhor/streaming-graph-ml/streaming-model-serving" \
                "/models/feedforward/torch/ffnn.torch"


def accuracy(outputs, labels):
    pass


class FeedForwardClassifier(BaseHandler, ABC):

    def __init__(self):
        super(FeedForwardClassifier, self).__init__()

    def initialize(self, ctx):
        input_size = 784
        num_classes = 10
        num_hidden_layers = 3
        hidden_size = 32

        self.device = 'cuda'

        self.model = model.FFNN(input_size, num_hidden_layers, hidden_size, out_size=num_classes,
                                accuracy_function=accuracy)
        state_dict = torch.load(MODEL_WEIGHTS)
        self.model.load_state_dict(state_dict)
        self.model.to(self.device)
        self.model.eval()

    def preprocess(self, requests):
        processed_imgs = []
        for req in requests:
            images = ast.literal_eval(req['body'].decode("utf-8"))
            images = np.array(images).astype(np.float32)
            processed_imgs.append(images)
        return np.array(processed_imgs)

    def inference(self, input_batch):
        input_batch = torch.from_numpy(input_batch)
        with torch.no_grad():
            input_batch = input_batch.to(self.device)
            out = self.model.forward(input_batch)
            #print(out)
            fake_output = ["0"] * len(input_batch)
            return fake_output

    def postprocess(self, inference_output):
        return inference_output

