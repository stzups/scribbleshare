import Client from './Client.js'
export default class LocalClient extends Client {
    constructor(id) {
        super(-1);
        this.point = null;
        this.lastTime = 0;
        this.lastSend = 0;
        this.refresh = true;
        this.points = [];
        console.log(this.point);
        canvas.addEventListener('mousedown', (event) => {
            this.lastTime = performance.now();
            this.points.push({
                dt:this.lastTime - this.lastSend,
                x:event.x,
                y:event.y,
            })
            this.point = {
                dt:0,
                x:0,
                y:0,
            }
        });
        canvas.addEventListener('mouseup', (event) => {
            if (this.point !== null) {
                this.pushPoint();
                this.point = null;
            }
        });
        canvas.addEventListener('mousemove', (event) => {
            if (this.point !== null) {
                ctx.beginPath();
                ctx.moveTo(event.x - event.movementX, event.y - event.movementY);
                ctx.lineTo(event.x, event.y);
                ctx.stroke();
                let now = performance.now();
                this.point.dt += now - this.lastTime;
                this.point.x += event.movementX;
                this.point.y += event.movementY;
                this.lastTime = now;
                
            }
        });
    }

    draw(dt) {

    }

    flushPoints() {
        
    }

    pushPoint() {
        if (this.point.dt !== 0 ||
            this.point.x !== 0 ||
            this.point.y !== 0) {
            this.points.push(this.point);
        }
    }

    getPoints() {
        this.lastSend = performance.now();
        if (this.point !== null) {
            this.pushPoint();

            this.point = {
                dt:0,
                x:0,
                y:0.
            };
        }
        this.points.forEach(p => console.log(p));
        
        console.log('asd');
        
        return this.points;
    }
}