export const canvas = document.getElementById('canvas');
export const ctx = canvas.getContext('2d');

import LocalClient from './LocalClient.js';
import SidebarItem from './SidebarItem.js';
import Client from './Client.js'
import socket from './WebSocketHandler.js'
import './InviteButton.js'

const documents = new Map();
var activeDocument = null;

export default class Document {
    constructor(name, id, points) {
        this.clients = new Map();
        this.name = name;
        this.id = id;
        this.sidebarItem = new SidebarItem(this.name, () => {
            if (this.id != null) {
                if (activeDocument != null) activeDocument.close();
                socket.sendOpen(this.id)
            } else {
                console.log('id was null', this);
            };
        });
        documents.set(this.id, this);
        this.points = points;
        //if (activeDocument == null) {
        //    this.open();
        //}
    }

    open() {
        activeDocument = this;
        console.log('opened ' + this.name);
        this.sidebarItem.setActive(false);
        window.history.pushState(document.name, document.title, '/d/' + this.id);
    }

    close() {
        localClient.update();
        ctx.clearRect(0, 0, canvas.width, canvas.height);//todo a loading screen?
    }

    draw(dt) {
        this.clients.forEach((client) => {
            client.draw(dt);
        });
    }
}

document.getElementById('add').addEventListener('click', () => {
    if (activeDocument != null) activeDocument.close();
    socket.sendCreate();
});

window.addEventListener('resize', resizeCanvas);
function resizeCanvas() {
    let imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);
    canvas.width = canvas.parentElement.offsetWidth;
    canvas.height = canvas.parentElement.offsetHeight;
    ctx.putImageData(imageData, 0, 0);
    //todo redraw?
};
resizeCanvas();

var last = performance.now();
function draw(now) {
    let dt = (now - last);
    last = now;

    if (activeDocument != null) {
        activeDocument.draw(dt);
    }

    window.requestAnimationFrame(draw);
}
window.requestAnimationFrame(draw);

socket.addEventListener('protocol.addclient', (event) => {
    let client = new Client(event.id);
    activeDocument.clients.set(client.id, client);
    console.log('Add client ', client);
});
socket.addEventListener('protocol.removeclient', (event) => {
    console.log('Remove client ', activeDocument.clients.delete(event.id));
});
socket.addEventListener('protocol.draw', (event) => {
    let client = activeDocument.clients.get(event.id);
    event.points.forEach((point) => {
        client.points.push(point);
    });
});
socket.addEventListener('protocol.adddocument', (event) => {
    documents.set(event.id, new Document(event.name, event.id));
});
socket.addEventListener('protocol.opendocument', (event) => {
    if (activeDocument != null) {
        activeDocument.close();
    }
    activeDocument = documents.get(event.id);
    activeDocument.open();
});
socket.addEventListener('protocol.handshake', (event) => {
    if (event.token != null) {
        window.localStorage.setItem('token', event.token.toString());
    }
})
socket.addEventListener('socket.open', (event) => {
    let token = window.localStorage.getItem('token');
    if (token != null) {
        token = BigInt(window.localStorage.getItem('token'));
    } else {
        token = BigInt(0);
    }
    socket.sendHandshake(token);
    let invite = document.location.href.substring(document.location.href.lastIndexOf("/") + 1);
    if (invite !== '') {
        socket.sendOpen(invite);
    }
});

const localClient = new LocalClient();