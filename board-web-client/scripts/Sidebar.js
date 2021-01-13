export default class Sidebar {
    constructor() {
        this.sidebar = document.getElementById('side');
        this.sidebarButtons = new Map();
        this.untitled = 0;
    }

    createButton(doc, active, onclick) {
        let button = document.createElement("button");
        let inner = document.createTextNode(doc.name);
        button.appendChild(inner);
        button.addEventListener('click', event => {
            this.setActive(event.target);
            onclick();
        });
        this.sidebar.appendChild(button);
        this.sidebarButtons.set(doc.id, button);
        if (active) {
            this.setActive(button);
        }
    }

    setActive(target) {
        this.sidebarButtons.forEach(sidebarButton => {
            if (target.innerHTML === sidebarButton.innerHTML) {
                sidebarButton.classList.add('active');
            } else {
                sidebarButton.classList.remove('active');
            }
        });
    }
}